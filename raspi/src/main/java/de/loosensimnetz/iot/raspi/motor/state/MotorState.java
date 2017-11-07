package de.loosensimnetz.iot.raspi.motor.state;

import de.loosensimnetz.iot.raspi.motor.MotorSensor;

public abstract class MotorState {
	public enum StateId {
		MOVING_DOWN,
		MOVING_UP,
		STOPPED_DOWN,
		STOPPED_INITIAL,
		ERROR
	};
	
	/**
	 * Transition from one state to the next.
	 * 
	 * @param sensor	New state
	 * @param updateTime Time of update in milliseconds
	 */
	protected void changeState(MotorSensor sensor, MotorState newState, long updateTime) {
		sensor.changeState(newState, updateTime);
	}
	
	/**
	 * State transition - delegates to the current state object
	 * 
	 * @param motor	Motor implementation
	 * @param updateTime Time of update in milliseconds
	 */
	public abstract void update(MotorSensor sensor, long updateTime);
	
	/**
	 * Return the name of this state
	 * 
	 * @return name of this state
	 */
	public abstract StateId getStateId();
}
