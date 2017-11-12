package de.loosensimnetz.iot.raspi.motor.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorSensor;

public class MotorStateError extends MotorState {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static MotorStateError instance = new MotorStateError();
	
	protected MotorStateError() {
		
	}
	
	public static MotorStateError instance() {
		return MotorStateError.instance;
	}

	@Override
	public void update(MotorSensor sensor, long updateTime) {
		final Motor motor = sensor.getMotor();
		
		if (motor.isMovingDown()) {
			// If the motor is moving down after an error, we are in the MovingDown state 
			logger.info("Motor starting to go down again at {} - changing state to MovingDown.", updateTime);
			
			changeState(sensor, MotorStateMovingDown.instance(), updateTime);
			return;
		}
		
		if (motor.isMovingUp()) {
			// If the motor is moving up after an error, we are in the MovingUp state 
			// this should not happen under ordinary circumstances - but you never know...
			logger.info("Motor starting to go down again at {} - changing state to MovingUp.", updateTime);
			
			changeState(sensor, MotorStateMovingUp.instance(), updateTime);
			return;
		}
		
		// If the motor is still not moving we are probably still in the error state
		return;
	}

	@Override
	public StateId getStateId() {
		return StateId.ERROR;
	}
}
