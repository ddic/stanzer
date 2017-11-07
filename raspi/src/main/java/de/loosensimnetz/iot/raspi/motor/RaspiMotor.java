package de.loosensimnetz.iot.raspi.motor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
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
	private final GpioPinDigitalInput sensorPin1;
	private final GpioPinDigitalInput sensorPin2;
	
	private ExpectedTime expectedTimeUp, expectedTimeDown, expectedTimeStoppedDown;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public RaspiMotor() {
		this(new ExpectedTime(7000L, 1000L), new ExpectedTime(7000L, 1000L));
	}
	
	@Override
	public ExpectedTime getExpectedTimeStoppedDown() {
		return expectedTimeStoppedDown;
	}

	@Override
	public void setExpectedTimeStoppedDown(ExpectedTime expectedTimeStoppedDown) {
		this.expectedTimeStoppedDown = expectedTimeStoppedDown;
	}



	public RaspiMotor(ExpectedTime maxTimeUp, ExpectedTime maxTimeDown) {
    	gpio = GpioFactory.getInstance();
    	
    	ledPin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.HIGH);
        ledPin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);
        
        sensorPin1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_08);
        sensorPin2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_09);
        
        ledPin1.setShutdownOptions(true, PinState.LOW);
        ledPin2.setShutdownOptions(true, PinState.LOW);
        
        this.expectedTimeDown = maxTimeDown;
        this.expectedTimeUp = maxTimeUp;
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
		
		logger.info("Setting expected time up to {}.", maxTimeDown);
	}

	@Override
	public boolean isMovingDown() {
		return sensorPin1.getState() == PinState.HIGH;
	}

	@Override
	public boolean isMovingUp() {
		return sensorPin2.getState() == PinState.HIGH;
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
