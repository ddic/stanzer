package de.loosensimnetz.iot.raspi.motor.state;

import de.loosensimnetz.iot.raspi.motor.Motor;

public class MotorStateError extends MotorState {
	private static MotorStateError instance = new MotorStateError();
	
	protected MotorStateError() {
		
	}
	
	public static MotorStateError instance() {
		return MotorStateError.instance;
	}

	@Override
	public void update(Motor motor, long updateTime) {
		if (motor.isMovingDown()) {
			// If the motor is moving down after an error, we are in the MovingDown state 
			changeState(MotorStateMovingDown.instance(), updateTime);
		}
		
		if (motor.isMovingUp()) {
			// If the motor is moving up after an error, we are in the MovingUp state 
			changeState(MotorStateMovingUp.instance(), updateTime);
		}
		
		// If the motor is still not moving we are probably still in the error state
		return;
	}

}
