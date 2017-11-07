package de.loosensimnetz.iot.raspi.opcua;

import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.MotorSensor;
import de.loosensimnetz.iot.raspi.motor.state.MotorStateStoppedInitial;

public class MotorResetStateMethod implements OpcMethod {
	private final MotorSensor motorSensor;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public MotorResetStateMethod(MotorSensor motorSensor) {
		this.motorSensor = motorSensor;
	}
	
    @UaMethod
    public void invoke(InvocationContext context) {
        logger.info("Resetting state");

        motorSensor.changeState(MotorStateStoppedInitial.instance(), System.currentTimeMillis());
    }
}