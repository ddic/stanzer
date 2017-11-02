package de.loosensimnetz.iot.raspi.motor.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorSensor;

public class MotorStateMovingDown extends MotorState {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final MotorStateMovingDown instance = new MotorStateMovingDown();
	
	protected MotorStateMovingDown() {
		
	}
	
	public static MotorStateMovingDown instance() {
		return MotorStateMovingDown.instance;
	}

	@Override
	public void update(MotorSensor sensor, long updateTime) {
		final Motor motor = sensor.getMotor();
		long timeElapsed = updateTime - sensor.getUpdateTime();		
		long earliestStateChange = motor.getExpectedTimeDown() - motor.getTolerance();
		long latestStateChange = motor.getExpectedTimeDown() + motor.getTolerance();
		
		if (!motor.isMovingDown() && timeElapsed < earliestStateChange) {
			// Motor stopped too early
			logger.info("Motor stopped too early - stop after {} ms, did not expect stop before {} ms.", timeElapsed, earliestStateChange);
			
			changeState(sensor, MotorStateError.instance(), updateTime);
			return;
		}
		
		if (motor.isMovingDown() && timeElapsed > latestStateChange) {
			// Motor is taking too long
			logger.info("Motor is taking too long - still running after {} ms, expect stop after {} ms.", timeElapsed, latestStateChange);
			
			changeState(sensor, MotorStateError.instance(), updateTime);
			return;
		}
		
		if (! motor.isMovingDown() && ! motor.isMovingUp() && timeElapsed <= latestStateChange) {
			// Motor stopped within tolerance
			logger.info("Motor stopped at {} within tolerance (from {} to {}) - changing state to MotorStateStoppedDown.", timeElapsed, earliestStateChange, latestStateChange);
			
			changeState(sensor, MotorStateStoppedDown.instance(), updateTime);
			return;
		}
		
		if (! motor.isMovingDown() && motor.isMovingUp() && timeElapsed >= earliestStateChange) {
			// Motor moving up within tolerance
			logger.info("Motor changed direction upward at {} within tolerance (from {} to {}) - changing state to MotorStateMovingUp.", timeElapsed, earliestStateChange, latestStateChange);
			
			changeState(sensor, MotorStateMovingUp.instance(), updateTime);
			return;
		}
		
		if (motor.isMovingDown() && timeElapsed <= latestStateChange) {
			// Motor is still on its way down - no state change
			return;
		}
		
		// Motor is in some kind of unexpected error state
		logger.info("Motor is in unexpected error state");
		
		changeState(sensor, MotorStateError.instance(), updateTime);
	}
}
