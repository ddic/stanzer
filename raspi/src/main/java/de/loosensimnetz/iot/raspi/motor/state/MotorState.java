package de.loosensimnetz.iot.raspi.motor.state;

import de.loosensimnetz.iot.raspi.motor.Motor;

public abstract class MotorState {
	/**
	 * Get  time of last status change in milliseconds
	 * 
	 * @return Time of last status change in Milliseconds
	 */
	protected long getUpdateTime() {
		return updateTime;
	}

	/**
	 * Set  time of last status change in milliseconds
	 * 
	 * @param updateTime Last status change in milliseconds
	 */
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * Time of last status change in milliseconds
	 */
	protected long updateTime;
	
	/**
	 * Current state of motor
	 */
	protected MotorState state;

	/**
	 * Transition from one state to the next. Convenience method - uses current
	 * system time.
	 * 
	 * @param state New state
	 */
	protected void changeState(MotorState state) {
		this.state = state;
		this.updateTime = System.currentTimeMillis();
	}
	
	/**
	 * Transition from one state to the next.
	 * 
	 * @param state	New state
	 * @param updateTime Time of update in milliseconds
	 */
	protected void changeState(MotorState state, long updateTime) {
		this.state = state;
		this.updateTime = updateTime;
	}

	/**
	 * Get current state
	 * 
	 * @return Current state
	 */
	public MotorState getState() {
		return state;
	}

	/**
	 * Set current state.
	 * 
	 * ATTENTION: for debugging purposes only - state is changed without state transition!
	 * @param state
	 */
	public void setState(MotorState state) {
		this.state = state;
	}
	
	/**
	 * State transition - delegates to the current state object
	 * 
	 * @param motor	Motor implementation
	 * @param updateTime Time of update in milliseconds
	 */
	public abstract void update(Motor motor, long updateTime);
}
