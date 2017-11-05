package de.loosensimnetz.iot.raspi.motor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import static de.loosensimnetz.iot.raspi.led.LedStateConverter.ledStateToPinState;
import static de.loosensimnetz.iot.raspi.led.LedStateConverter.pinStateToLedState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaspiMotor implements Motor {
	
	private final GpioController gpio;
	private final GpioPinDigitalOutput ledPin1;
	private final GpioPinDigitalOutput ledPin2;
	private long expectedTimeUp, expectedTimeDown, expectedTimeStoppedDown, tolerance;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public RaspiMotor() {
		this(8000L, 8000L);
	}
	
	@Override
	public long getExpectedTimeStoppedDown() {
		return expectedTimeStoppedDown;
	}

	@Override
	public void setExpectedTimeStoppedDown(long expectedTimeStoppedDown) {
		this.expectedTimeStoppedDown = expectedTimeStoppedDown;
	}



	public RaspiMotor(long maxTimeUp, long maxTimeDown) {
    	gpio = GpioFactory.getInstance();
    	
    	ledPin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.HIGH);
        ledPin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);
        
        ledPin1.setShutdownOptions(true, PinState.LOW);
        ledPin2.setShutdownOptions(true, PinState.LOW);
        
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
		
		logger.info("Setting expected time up to {}.", maxTimeDown);
	}

	@Override
	public boolean isMovingDown() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMovingUp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LedState getLed1State() {
		return pinStateToLedState(ledPin1);
	}

	@Override
	public LedState getLed2State() {
		return pinStateToLedState(ledPin2);
	}

	@Override
	public void setLed1State(LedState state) {		
		ledPin1.setState(ledStateToPinState(state));
	}

	@Override
	public void setLed2State(LedState state) {
		ledPin2.setState(ledStateToPinState(state));
	}
}
