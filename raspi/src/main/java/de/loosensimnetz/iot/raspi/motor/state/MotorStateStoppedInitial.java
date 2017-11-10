package de.loosensimnetz.iot.raspi.motor.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;
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
		final Motor motor = sensor.getMotor();
		final boolean motorMovingDown = motor.isMovingDown() && !motor.isMovingUp();
		final boolean motorMovingUp = !motor.isMovingDown() && motor.isMovingUp();
		final boolean motorStopped = !motorMovingUp && !motorMovingDown;

		if (motorStopped) {
			// Motor is still not moving - no state change
			return;
		}

		if (motorMovingDown) {
			// From the initial state the motor can only move down - MovingDown state
			logger.info("Motor is starting to move down at {} ms - changing state to MovingDown", updateTime);

			changeState(sensor, MotorStateMovingDown.instance(), updateTime);
			return;
		}

		// We must be in an error state
		// Motor is in some kind of unexpected error state
		logger.info("Motor is in unexpected error state. MovingUp: {}, movingDown: {}.", motorMovingUp,
				motorMovingDown);

		changeState(sensor, MotorStateError.instance(), updateTime);
	}

	@Override
	public StateId getStateId() {
		return StateId.STOPPED_INITIAL;
	}
}
