package de.loosensimnetz.iot.raspi.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.Motor.LedState;

public class MockMotor implements Motor {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean movingDown, movingUp;
	
	private LedState led1State, led2State;
	
	public MockMotor() {
		this(false, false, LedState.OFF, LedState.OFF);
	}

	public MockMotor(boolean movingDown, boolean movingUp, LedState led1State, LedState led2State) {
		super();
		this.movingDown = movingDown;
		this.movingUp = movingUp;
		this.led1State = led1State;
		this.led2State = led2State;
	}

	@Override
	public boolean isMovingDown() {
		return this.movingDown;
	}

	@Override
	public boolean isMovingUp() {
		return this.movingUp;
	}

	@Override
	public LedState getLed1State() {
		return this.led1State;
	}

	@Override
	public LedState getLed2State() {
		return this.led2State;
	}

	@Override
	public void setLed1State(LedState state) {
		this.led1State = state;
		
		logger.info("Setting state of led #1 to {}.", state.toString());
	}

	@Override
	public void setLed2State(LedState state) {
		this.led2State = state;
		
		logger.info("Setting state of led #2 to {}.", state.toString());
	}

}
