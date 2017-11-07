package de.loosensimnetz.iot.raspi.motor;

import de.loosensimnetz.iot.raspi.motor.state.MotorState;
import de.loosensimnetz.iot.raspi.motor.state.MotorStateStoppedInitial;

/**
 * Class modeling the values for the motor sensor. Implements the GoF state pattern.
 * 
 * @author jloosen
 *
 */
public class MotorSensor {
	/**
	 * The current state of the motor
	 */
	private MotorState state;
	/**
	 * Reference to the current motor
	 */
	private Motor motor;
	/**
	 * Last update of the motor state in milliseconds
	 */
	private long updateTime;
	
	/**
	 * Constructor
	 * 
	 * @param motor Reference to the current motor
	 * @param initialTime Initial time in milliseconds (usually System.currentTimeMillis())
	 */
	public MotorSensor(Motor motor, long initialTime) {
		this.state = MotorStateStoppedInitial.instance();
		this.motor = motor;
		this.updateTime = initialTime;
	}
	
	/**
	 * Update the current state according to the state model
	 * 
	 * @param newState New state
	 * @param updateTime Time of update in milliseconds
	 */
	public void changeState(MotorState newState, long updateTime) {
		this.state = newState;
		this.updateTime = updateTime;
	}
	
	/**
	 * Return the current state of the motor sensor
	 * 
	 * @return Current state of the motor sensor
	 */
	public MotorState getState() {
		return state;
	}

	/**
	 * Set the current state of the motor sensor
	 * 
	 * @param state new State
	 */
	public void setState(MotorState state) {
		this.state = state;
	}

	/**
	 * Return the current motor
	 * 
	 * @return Current motor
	 */
	public Motor getMotor() {
		return motor;
	}
	
	/**
	 * Set the current motor
	 * 
	 * @param motor Current motor
	 */
	public void setMotor(Motor motor) {
		this.motor = motor;
	}

	/**
	 * Return the last update time in milliseconds
	 * @return Last update time in milliseconds
	 */
	public long getUpdateTime() {
		return updateTime;
	}
	
	/**
	 * Set the last update time in milliseconds
	 * @param updateTime Last update time in milliseconds
	 */
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	
	/**
	 * Update the current state according to the state model. See GoF state pattern
	 * 
	 * @param updateTime Update time in milliseconds
	 */
	public void update(long updateTime) {
		state.update(this, updateTime);
	}
	
	/**
	 * Return the id of the current state as a string
	 * 
	 * @return Current state as a string
	 */
	public String getStateId() {
		return state.getStateId().toString();
	}
}
