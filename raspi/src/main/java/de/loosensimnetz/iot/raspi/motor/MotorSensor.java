package de.loosensimnetz.iot.raspi.motor;

import de.loosensimnetz.iot.raspi.motor.state.MotorState;
import de.loosensimnetz.iot.raspi.motor.state.MotorStateStoppedInitial;

public class MotorSensor {
	private MotorState state;
	private Motor motor;
	private long updateTime;
	
	public MotorSensor(Motor motor, long updateTime) {
		this.state = MotorStateStoppedInitial.instance();
		this.motor = motor;
		this.updateTime = updateTime;
	}
	
	public void changeState(MotorState newState, long updateTime) {
		this.state = newState;
		this.updateTime = updateTime;
	}
	
	public MotorState getState() {
		return state;
	}

	public void setState(MotorState state) {
		this.state = state;
	}

	public Motor getMotor() {
		return motor;
	}

	public void setMotor(Motor motor) {
		this.motor = motor;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	public void update(long updateTime) {
		state.update(this, updateTime);
	}
}
