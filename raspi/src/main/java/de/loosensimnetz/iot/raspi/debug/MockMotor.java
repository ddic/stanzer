package de.loosensimnetz.iot.raspi.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.ExpectedTime;
import de.loosensimnetz.iot.raspi.motor.Motor;

public class MockMotor implements Motor {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean movingDown, movingUp;	
	private LedState led1State, led2State;
	private ExpectedTime expectedTimeUp, expectedTimeDown, expectedTimeStoppedDown;
	
	public MockMotor() {
		this(false, false, LedState.OFF, LedState.OFF, new ExpectedTime(5000L, 0L), new ExpectedTime(5000L, 0L), new ExpectedTime(1000L, 0L));
	}

	public void setMovingDown(boolean movingDown) {
		this.movingDown = movingDown;
	}
	
	@Override
	public ExpectedTime getExpectedTimeStoppedDown() {
		return expectedTimeStoppedDown;
	}

	@Override
	public void setExpectedTimeStoppedDown(ExpectedTime expectedTimeStoppedDown) {
		this.expectedTimeStoppedDown = expectedTimeStoppedDown;
	}

	public void setMovingUp(boolean movingUp) {
		this.movingUp = movingUp;
	}

	public MockMotor(boolean movingDown, boolean movingUp, LedState led1State, LedState led2State, ExpectedTime expectedTimeUp, ExpectedTime expectedTimeDown, ExpectedTime expectedTimeStoppedDown) {
		super();
		this.movingDown = movingDown;
		this.movingUp = movingUp;
		this.led1State = led1State;
		this.led2State = led2State;
		this.expectedTimeDown = expectedTimeDown;
		this.expectedTimeUp = expectedTimeUp;
		this.expectedTimeStoppedDown = expectedTimeStoppedDown;
	}

	public MockMotor(ExpectedTime maxTimeUp, ExpectedTime maxTimeDown) {
		this(false, false, LedState.OFF, LedState.OFF, maxTimeUp, maxTimeDown, new ExpectedTime(1000L, 0L));
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
	public LedState getLed12State() {
		return this.led1State;
	}

	@Override
	public LedState getLed13State() {
		return this.led2State;
	}

	@Override
	public void setLed12State(LedState state) {
		this.led1State = state;
		
		logger.info("Setting state of led #1 to {}.", state.toString());
	}

	@Override
	public void setLed13State(LedState state) {
		this.led2State = state;
		
		logger.info("Setting state of led #2 to {}.", state.toString());
	}

	@Override
	public ExpectedTime getExpectedTimeUp() {
		return expectedTimeUp;
	}

	@Override
	public void setExpectedTimeUp(ExpectedTime maxTimeUp) {
		this.expectedTimeUp = maxTimeUp;
		
		logger.info("Setting expected time up to {}.", maxTimeUp);
	}

	@Override
	public ExpectedTime getExpectedTimeDown() {
		return expectedTimeDown;
	}

	@Override
	public void setExpectedTimeDown(ExpectedTime maxTimeDown) {
		this.expectedTimeDown = maxTimeDown;
		
		logger.info("Setting expected time down to {}.", maxTimeDown);
	}	
}
