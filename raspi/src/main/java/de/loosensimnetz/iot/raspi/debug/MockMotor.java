package de.loosensimnetz.iot.raspi.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;

public class MockMotor implements Motor {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean movingDown, movingUp;	
	private LedState led1State, led2State;
	private long expectedTimeUp, expectedTimeDown, tolerance;
	
	public MockMotor() {
		this(false, false, LedState.OFF, LedState.OFF, 8000L, 8000L);
	}

	public MockMotor(boolean movingDown, boolean movingUp, LedState led1State, LedState led2State, long maxTimeUp, long maxTimeDown) {
		super();
		this.movingDown = movingDown;
		this.movingUp = movingUp;
		this.led1State = led1State;
		this.led2State = led2State;
		this.expectedTimeDown = maxTimeDown;
		this.expectedTimeUp = maxTimeUp;
	}
	
	@Override
	public long getTolerance() {
		return tolerance;
	}

	@Override
	public void setTolerance(long tolerance) {
		this.tolerance = tolerance;
		
		logger.info("Setting tolerance to {}.", tolerance);
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

	@Override
	public long getExpectedTimeUp() {
		return expectedTimeUp;
	}

	@Override
	public void setExpectedTimeUp(long maxTimeUp) {
		this.expectedTimeUp = maxTimeUp;
		
		logger.info("Setting expected time up to {}.", maxTimeUp);
	}

	@Override
	public long getExpectedTimeDown() {
		return expectedTimeDown;
	}

	@Override
	public void setExpectedTimeDown(long maxTimeDown) {
		this.expectedTimeDown = maxTimeDown;
		
		logger.info("Setting expected time down to {}.", maxTimeDown);
	}	
}
