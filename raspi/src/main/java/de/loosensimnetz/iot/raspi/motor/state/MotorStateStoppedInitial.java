package de.loosensimnetz.iot.raspi.motor.state;

import de.loosensimnetz.iot.raspi.motor.Motor;

public class MotorStateStoppedInitial extends MotorState {
	private static final MotorStateStoppedInitial instance = new MotorStateStoppedInitial();
	
	protected MotorStateStoppedInitial() {		
	}

	public static MotorStateStoppedInitial instance() {
		return MotorStateStoppedInitial.instance;
	}

	@Override
	public void update(Motor motor, long updateTime) {
		if (! motor.isMovingDown() && ! motor.isMovingUp()) {
			// Motor is still not moving - no state change
			return;
		}
		
		if (motor.isMovingDown()) {
			// From the initial state the motor can only move down - MovingDown state
			changeState(MotorStateMovingDown.instance(), updateTime);
		}
		
		// We must be in an error state
		changeState(MotorStateError.instance(), updateTime);
	}
}
