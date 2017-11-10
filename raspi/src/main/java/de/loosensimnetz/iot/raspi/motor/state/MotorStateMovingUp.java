package de.loosensimnetz.iot.raspi.motor.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorSensor;

public class MotorStateMovingUp extends MotorState {
	private static final MotorStateMovingUp instance = new MotorStateMovingUp();
	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected MotorStateMovingUp() {

	}

	public static MotorStateMovingUp instance() {
		return MotorStateMovingUp.instance;
	}

	@Override
	public void update(MotorSensor sensor, long updateTime) {
		final Motor motor = sensor.getMotor();
		long timeElapsed = updateTime - sensor.getUpdateTime();
		long earliestStateChange = motor.getExpectedTimeUp().getExpectedTime()
				- motor.getExpectedTimeUp().getTolerance();
		long latestStateChange = motor.getExpectedTimeUp().getExpectedTime() + motor.getExpectedTimeUp().getTolerance();

		final boolean motorMovingDown = motor.isMovingDown() && !motor.isMovingUp();
		final boolean motorMovingUp = !motor.isMovingDown() && motor.isMovingUp();
		final boolean motorStopped = !motorMovingUp && !motorMovingDown;

		if (motorStopped && timeElapsed < earliestStateChange) {
			// Motor stopped too early
			logger.info("Motor stopped too early - stop after {} ms, did not expect stop before {} ms.", timeElapsed,
					earliestStateChange);

			changeState(sensor, MotorStateError.instance(), updateTime);
			return;
		}

		if (motorMovingUp && timeElapsed > latestStateChange) {
			// Motor is taking too long
			logger.info("Motor is taking too long - still running after {} ms, expect stop after {} ms.", timeElapsed,
					latestStateChange);

			changeState(sensor, MotorStateError.instance(), updateTime);
			return;
		}

		if (motorMovingUp && timeElapsed <= latestStateChange) {
			// Motor is still on its way Up - no state change
			return;
		}

		if (motorStopped && timeElapsed <= latestStateChange && timeElapsed >= earliestStateChange) {
			logger.info(
					"Motor stopped at {} within tolerance (from {} to {}) - changing state to MotorStateStoppedInitial.",
					timeElapsed, earliestStateChange, latestStateChange);

			changeState(sensor, MotorStateStoppedInitial.instance(), updateTime);
			return;
		}

		// Motor is in some kind of unexpected error state
		logger.info(
				"Motor is in unexpected error state. Time elapsed: {}, earliestStateChange: {}, "
						+ "latestStateChange: {}, movingUp: {}, movingDown: {}.",
				timeElapsed, earliestStateChange, latestStateChange, motorMovingUp, motorMovingDown);

		changeState(sensor, MotorStateError.instance(), updateTime);
	}

	@Override
	public StateId getStateId() {
		return StateId.MOVING_UP;
	}
}
