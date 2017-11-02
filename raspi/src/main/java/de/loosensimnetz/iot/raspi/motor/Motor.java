package de.loosensimnetz.iot.raspi.motor;

public interface Motor {
	
	/**
	 * Get the tolerance in milliseconds where the duration of a movement (i. e. down or up) is still acceptable
	 *  
	 * @return Tolerance in milliseconds 
	 */
	long getTolerance();
	
	/**
	 * Set the tolerance in milliseconds where the duration of a movement (i. e. down or up) is still acceptable
	 * 
	 * @param tolerance Tolerance in milliseconds 
	 */
	void setTolerance(long tolerance);
	
	/**
	 * Get the expected time in milliseconds for the motor to move from the initial position downward
	 * to the lower end position
	 * 
	 * @return
	 */
	long getExpectedTimeDown();
	
	/**
	 * Set the expected time in milliseconds for the motor to move from the initial position downward
	 * to the lower end position
	 * 
	 * @param expectedTimeUp Expected time in milliseconds for the motor to move from the initial position downward
	 * to the lower end position
	 */
	void setExpectedTimeDown(long expectedTimeDown);
	
	/**
	 * Get the expected time in milliseconds for the motor to move from the lower end position upward
	 * to the initial position
	 * 
	 * @return
	 */
	long getExpectedTimeUp();
	
	/**
	 * Set the expected time in milliseconds for the motor to move from the lower end position upward
	 * to the initial position	
	 * 
	 * @param expectedTimeUp Expected time in milliseconds for the motor to move from the lower end position upward
	 * to the initial position
	 */
	void setExpectedTimeUp(long expectedTimeUp);

	/**
	 * Is the motor moving downward?
	 * 
	 * @return <code>true</code>, if motor is moving downward. <code>false</code> otherwise.
	 */
	boolean isMovingDown();

	/**
	 * Is the motor moving upward?
	 * 
	 * @return <code>true</code>, if motor is moving upward. <code>false</code> otherwise.
	 */
	boolean isMovingUp();
	
	/**
	 * An LED has two states: ON or OFF.
	 *
	 */
	public static enum LedState { ON, OFF };
	
	/**
	 * Get {@link Motor#LedState} of LED #1
	 * 
	 * @return the LedState of LED #1
	 */
	public LedState getLed1State();
	
	/**
	 * Get {@link Motor#LedState} of LED #2
	 * 
	 * @return the LedState of LED #2
	 */
	public LedState getLed2State();
	
	/**
	 * Set the LedState of LED #1
	 * 
	 * @param state the state of LED #1 after this method returns
	 */
	public void setLed1State(LedState state);
	
	/**
	 * Set the LedState of LED #2
	 * 
	 * @param state the state of LED #2 after this method returns
	 */
	public void setLed2State(LedState state);
	
}