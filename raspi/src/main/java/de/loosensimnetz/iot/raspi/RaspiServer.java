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

import de.loosensimnetz.iot.raspi.motor.ExpectedTime;
import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorFactory;
import de.loosensimnetz.iot.raspi.motor.MotorSensor;
import de.loosensimnetz.iot.raspi.motor.MotorSensorMonitor;
import de.loosensimnetz.iot.raspi.opcua.KeyStoreLoader;
import de.loosensimnetz.iot.raspi.opcua.RaspiServerNamespace;

/**
 * Raspberry server wirh OPC UA capabilities
 *
 */
public class RaspiServer {
	public static final String TEXT_MANUFACTURER_NAME = "www.loosensimnetz.de";
	public static final String TEXT_APPLICATION_NAME = "J. Loosen IoT Raspi Server";
	public static final String TEXT_APPLICATION_URI = "urn:loosensimnetz.de:iot:raspi:server";
	public static final String TEXT_RASPI_SERVER = "IoT-RaspiServer";

	private static void usage() {
		System.out.format("sudo java -jar  \\\n" + 
				"	 	-Dde.loosensimnetz.iot.raspi.MotorFactory=[de.loosensimnetz.iot.raspi.motor.DefaultMotorFactory]  \\\n" + 
				"	 	-Dde.loosensimnetz.iot.raspi.ExpectedTimeDown=[5000]  \\\n" + 
				"	 	-Dde.loosensimnetz.iot.raspi.ExpectedTimeUp=[5000]  \\\n");
	}
	
	/**
	 * Main entry for the application.
	 * 
	 * Start of server:
	 * 
	 * sudo java -jar  \
	 * 			 -Dde.loosensimnetz.iot.raspi.MotorFactory=[de.loosensimnetz.iot.raspi.motor.DefaultMotorFactory]  \
	 * 		     -Dde.loosensimnetz.iot.raspi.ExpectedTimeDown=[5000]  \
	 *           -Dde.loosensimnetz.iot.raspi.ExpectedTimeUp=[5000]  \
	 *           <arg[0]>
	 *           
	 * @param args	Command line arguments (not evaluated)
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		RaspiServer server = new RaspiServer();
		usage();

        server.startup().get();

        final CompletableFuture<Void> future = new CompletableFuture<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> future.complete(null)));

        future.get();
    }

    /**
     * Eclipse milo OPC UA server
     */
	private final OpcUaServer server;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * The motor - can be MockMotor or RaspiMotor
     */
	private final Motor motor;
	/**
	 * The motor sensor
	 */
	private final MotorSensor motorSensor;
	/**
	 * The motor sensor monitor (daemon thread)
	 */
	private final MotorSensorMonitor motorSensorMonitor;

    /**
     * COnstructor
     * @throws Exception
     */
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
    
    /**
     * Parse java environment parameter as long value
     * 
     * @param parameterName Name of the parameter
     * @param def Default value - used if no parameter is supplied or supplied parameter has an invalid value
     * @return
     */
	private long getLongParameter(String parameterName, long def) {
    	long result;
    	String value = System.getProperty(parameterName);
    	
    	if (value == null) {
    		logger.info("No value found for parameter {} - using default {}.", parameterName, def);
    		result = def;
    	}
    	else {
    		try {
    			result = Long.parseLong(value);
    			logger.info("Using supplied value {} for parameter {}", result, parameterName);
    		}
    		catch(NumberFormatException e) {
    			logger.info("Invalid value {} for parameter {} - using default {}.", value, parameterName, def);
    			result = def;
    		}
    	}
    	
    	return result;
    }

	/**
	 * Create motor instance based on factory class supplied in java environment variable.
	 * 
	 * @return
	 */
	private Motor createMotor() {
		// get the values for expected time down and time up from environment variables - or use default
		long etd = getLongParameter(RaspiConstants.PROPERTY_TIME_DOWN, RaspiConstants.PROPERTY_VALUE_TIME_DOWN),
		     etu = getLongParameter(RaspiConstants.PROPERTY_TIME_UP, RaspiConstants.PROPERTY_VALUE_TIME_UP);
		
		String motorFactoryName = 
				System.getProperty(RaspiConstants.PROPERTY_MOTORFACTORY, 
						RaspiConstants.PROPERTY_VALUE_DEFAULT_MOTORFACTORY);
		
		try {
			MotorFactory motorFactory = (MotorFactory) Class.forName(motorFactoryName).newInstance();
			Motor result = motorFactory.createMotor(new ExpectedTime(etu, 1000L), new ExpectedTime(etd, 1000L));
			
			logger.info("Instanciated motor class {} with expected time down {} and time up {}", result.getClass().getName(), etd, etu);
			
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
