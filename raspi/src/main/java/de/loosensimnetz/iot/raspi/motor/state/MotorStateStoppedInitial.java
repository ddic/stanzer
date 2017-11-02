package de.loosensimnetz.iot.raspi.motor.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.MotorSensor;

public class MotorStateStoppedInitial extends MotorState {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final MotorStateStoppedInitial instance = new MotorStateStoppedInitial();
	
	protected MotorStateStoppedInitial() {
		
	}

	public static MotorStateStoppedInitial instance() {
		return MotorStateStoppedInitial.instance;
	}

	@Override
	public void update(MotorSensor sensor, long updateTime) {		
		if (! sensor.getMotor().isMovingDown() && ! sensor.getMotor().isMovingUp()) {
			// Motor is still not moving - no state change
			return;
		}
		
		if (sensor.getMotor().isMovingDown()) {
			// From the initial state the motor can only move down - MovingDown state
			logger.info("Motor ist starting to move down at {} ms - changing state to MovingDown", updateTime);
			
			changeState(sensor, MotorStateMovingDown.instance(), updateTime);
			return;
		}
		
		// We must be in an error state
		logger.info("Motor ist in unexpected error state at {} ms", updateTime);
		
		changeState(sensor, MotorStateError.instance(), updateTime);
	}
}
