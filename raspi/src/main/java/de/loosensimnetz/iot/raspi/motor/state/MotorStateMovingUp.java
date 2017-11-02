package de.loosensimnetz.iot.raspi.motor.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;

public class MotorStateMovingUp extends MotorState {
	private static final MotorStateMovingUp instance = new MotorStateMovingUp();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected MotorStateMovingUp() {
		
	}
	
	public static MotorStateMovingUp instance() {
		return MotorStateMovingUp.instance;
	}

	@Override
	public void update(Motor motor, long updateTime) {
		long timeElapsed = this.getUpdateTime() - updateTime;
		long earliestStateChange = motor.getExpectedTimeUp() - motor.getTolerance();
		long latestStateChange = motor.getExpectedTimeUp() + motor.getTolerance();
		
		if (!motor.isMovingUp() && timeElapsed < earliestStateChange) {
			// Motor stopped too early
			logger.info("Motor stopped to early - stop after {} ms, did not expect stop before {} ms.", timeElapsed, earliestStateChange);
			
			changeState(MotorStateError.instance(), updateTime);
		}
		
		if (motor.isMovingUp() && timeElapsed > latestStateChange) {
			// Motor is taking too long
			logger.info("Motor is taking too long - still running after {} ms, expect stop after {} ms.", timeElapsed, latestStateChange);
			
			changeState(MotorStateError.instance(), updateTime);
		}
		
		if (motor.isMovingUp() && timeElapsed < latestStateChange) {
			// Motor is still on its way Up - no state change
			return;
		}
		
		// Motor is in some kind of unexpected error state
		logger.info("Motor is in unexpected error state");
		
		changeState(MotorStateError.instance(), updateTime);
	}

}
