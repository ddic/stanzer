package de.loosensimnetz.iot.raspi;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.stack.core.application.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.application.DefaultCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.ResponseHeader;
import org.eclipse.milo.opcua.stack.core.types.structured.TestStackExRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.TestStackExResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.TestStackRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.TestStackResponse;
import org.eclipse.milo.opcua.stack.core.util.CryptoRestrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import de.loosensimnetz.iot.RaspiConstants;
import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorFactory;
import de.loosensimnetz.iot.raspi.motor.MotorSensor;
import de.loosensimnetz.iot.raspi.motor.MotorSensorMonitor;
import de.loosensimnetz.iot.raspi.opcua.KeyStoreLoader;
import de.loosensimnetz.iot.raspi.opcua.RaspiServerNamespace;

/**
 * Hello world!
 *
 */
public class RaspiServer {
	public static final String TEXT_MANUFACTURER_NAME = "www.loosensimnetz.de";
	public static final String TEXT_APPLICATION_NAME = "J. Loosen IoT Raspi Server";
	public static final String TEXT_APPLICATION_URI = "urn:loosensimnetz.de:iot:raspi:server";
	public static final String TEXT_RASPI_SERVER = "IoT-RaspiServer";

	public static void main(String[] args) throws Exception {
		RaspiServer server = new RaspiServer();

        server.startup().get();

        final CompletableFuture<Void> future = new CompletableFuture<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));

        future.get();
    }

    private final OpcUaServer server;
    private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Motor motor;
	private final MotorSensor motorSensor;
	private final MotorSensorMonitor motorSensorMonitor;

    public RaspiServer() throws Exception {
        CryptoRestrictions.remove();

        KeyStoreLoader loader = new KeyStoreLoader().load();

        DefaultCertificateManager certificateManager = new DefaultCertificateManager(
            loader.getServerKeyPair(),
            loader.getServerCertificate()
        );

        File securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security");

        logger.info("security temp dir: {}", securityTempDir.getAbsolutePath());

        DefaultCertificateValidator certificateValidator = new DefaultCertificateValidator(securityTempDir);

        UsernameIdentityValidator identityValidator = new UsernameIdentityValidator(
            true,
            authChallenge -> {
                String username = authChallenge.getUsername();
                String password = authChallenge.getPassword();
                
                logger.info("Login attempt with username {}", username);

                boolean userOk = "user".equals(username) && "password1".equals(password);
                boolean adminOk = "admin".equals(username) && "password2".equals(password);
            
                return userOk || adminOk;
            }
        );

        OpcUaServerConfig serverConfig = OpcUaServerConfig.builder()
            .setApplicationUri(TEXT_APPLICATION_URI)
            .setApplicationName(LocalizedText.english(TEXT_APPLICATION_NAME))
            .setBindAddresses(newArrayList("0.0.0.0"))
            .setBindPort(12686)
            .setBuildInfo(
                new BuildInfo(
                    TEXT_APPLICATION_URI,
                    TEXT_MANUFACTURER_NAME,
                    TEXT_RASPI_SERVER,
                    OpcUaServer.SDK_VERSION,
                    "", DateTime.now()))
            .setCertificateManager(certificateManager)
            .setCertificateValidator(certificateValidator)
            .setIdentityValidator(identityValidator)
            .setProductUri(TEXT_APPLICATION_URI)
            .setServerName(TEXT_RASPI_SERVER)
            .setSecurityPolicies(
                EnumSet.of(
                    SecurityPolicy.None,
                    SecurityPolicy.Basic128Rsa15,
                    SecurityPolicy.Basic256,
                    SecurityPolicy.Basic256Sha256))
            .setUserTokenPolicies(
                ImmutableList.of(
                    USER_TOKEN_POLICY_ANONYMOUS,
                    USER_TOKEN_POLICY_USERNAME))
            .build();

        server = new OpcUaServer(serverConfig);
        motor = createMotor();
        motorSensor = new MotorSensor(motor, System.currentTimeMillis());
        motorSensorMonitor = new MotorSensorMonitor(250L, motorSensor);
        
        server.getNamespaceManager().registerAndAdd(
            RaspiServerNamespace.NAMESPACE_URI,
            idx -> new RaspiServerNamespace(server, idx, motor, motorSensor));

        server.getServer().addRequestHandler(TestStackRequest.class, service -> {
            TestStackRequest request = service.getRequest();

            ResponseHeader header = service.createResponseHeader();

            service.setResponse(new TestStackResponse(header, request.getInput()));
        });

        server.getServer().addRequestHandler(TestStackExRequest.class, service -> {
            TestStackExRequest request = service.getRequest();

            ResponseHeader header = service.createResponseHeader();

            service.setResponse(new TestStackExResponse(header, request.getInput()));
        });
    }

	private Motor createMotor() {
		String motorFactoryName = 
				System.getProperty(RaspiConstants.PROPERTY_MOTORFACTORY, 
						RaspiConstants.PROPERTY_VALUE_DEFAULT_MOTORFACTORY);
			
		try {
			MotorFactory motorFactory = (MotorFactory) Class.forName(motorFactoryName).newInstance();
			Motor result = motorFactory.createMotor();
			
			logger.info("Instanciated motor class {}", result.getClass().getName());
			
			return result;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			e.printStackTrace(pw);
			
			logger.error("Could not instanciate MotorFactory class {}:\n{}.", motorFactoryName, sw.toString());
			
			throw new RuntimeException("Could not instanciate MotorFactory class");
		}
	}

	public OpcUaServer getServer() {
        return server;
    }

    public CompletableFuture<OpcUaServer> startup() {
    	motorSensorMonitor.start();
        return server.startup();
    }

    public CompletableFuture<OpcUaServer> shutdown() {
    	motorSensorMonitor.interrupt();
    	return server.shutdown();
    }
    
    
}
