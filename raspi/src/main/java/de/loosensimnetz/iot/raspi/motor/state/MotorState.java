package de.loosensimnetz.iot.raspi.motor.state;

import de.loosensimnetz.iot.raspi.motor.MotorSensor;

public abstract class MotorState {	
	/**
	 * Time of last status change in milliseconds
	 */
	protected long updateTime;
	
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
}
