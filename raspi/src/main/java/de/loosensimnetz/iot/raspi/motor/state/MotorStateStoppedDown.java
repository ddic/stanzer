package de.loosensimnetz.iot.raspi.motor.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorSensor;

public class MotorStateStoppedDown extends MotorState {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final MotorStateStoppedDown instance = new MotorStateStoppedDown();
	
	protected MotorStateStoppedDown() {
		
	}

	public static MotorStateStoppedDown instance() {
		return MotorStateStoppedDown.instance;
	}

	@Override
	public void update(MotorSensor sensor, long updateTime) {
		final Motor motor = sensor.getMotor();
		final boolean motorMovingDown = motor.isMovingDown() && !motor.isMovingUp();
		final boolean motorMovingUp = !motor.isMovingDown() && motor.isMovingUp();
		final boolean motorStopped = !motorMovingUp && !motorMovingDown;
		
		if (motorStopped) {
			// Motor is still not moving - no state change
			return;
		}
		
		if (motorMovingUp) {
			// From the down state the motor can only move up - MovingUp state
			logger.info("Motor ist starting to move up at {} ms - changing state to MovingUp", updateTime);
			
			changeState(sensor, MotorStateMovingUp.instance(), updateTime);
			return;
		}
		
		// We must be in an error state
		logger.info("Motor ist in unexpected error state at {} ms", updateTime);
		
		changeState(sensor, MotorStateError.instance(), updateTime);
	}
	
	@Override
	public StateId getStateId() {
		return StateId.STOPPED_DOWN;
	}
}
