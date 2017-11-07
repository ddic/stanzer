package de.loosensimnetz.iot.raspi.opcua;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.MotorSensor;

public class MotorStateString {
	private final MotorSensor motorSensor;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public MotorStateString(MotorSensor motorSensor) {
		this.motorSensor = motorSensor;
	}
	    
    public String getStateId() {
        logger.debug("Returning the id of motor state.");
        
    	return motorSensor.getStateId();
    }

}
