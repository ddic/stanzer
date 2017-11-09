package de.loosensimnetz.iot.raspi.opcua;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.Motor.LedState;

public class LedStateBoolean {
	private final Motor motor;
	private final int ledNumber;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public LedStateBoolean(Motor motor, int ledNumber) {
		this.motor = motor;
		this.ledNumber = ledNumber;
	}
	    
    public boolean getState() {

        logger.debug("Returning the value of ledState{}.", ledNumber);
        
        if (ledNumber == 1) {
        	return motor.getLed12State() == LedState.ON;
        }
        
        if (ledNumber == 2) {
        	return motor.getLed13State() == LedState.ON;
        }
        
        return false;
    }

}
