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
	private final GpioPinDigitalOutput ledPin12;
	private final GpioPinDigitalOutput ledPin13;
	private final GpioPinDigitalInput sensorPinU2;
	private final GpioPinDigitalInput sensorPinU3;
	
	private ExpectedTime expectedTimeUp, expectedTimeDown, expectedTimeStoppedDown;
	
	private static final Logger logger = LoggerFactory.getLogger(RaspiMotor.class);

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
    	
    	logger.info("Connection led 12 to GPIO pin 02 (WiringPI)");
    	ledPin12 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.HIGH);
    	logger.info("Connection led 13 to GPIO pin 00 (WiringPI)");
    	ledPin13 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);

    	logger.info("Connection sensor U2 to GPIO pin 04 (WiringPI)");
        sensorPinU2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04);
        logger.info("Connection sensor U3 to GPIO pin 05 (WiringPI)");
        sensorPinU3 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05);
        
        ledPin12.setShutdownOptions(true, PinState.LOW);
        ledPin13.setShutdownOptions(true, PinState.LOW);
        
        sensorPinU2.setShutdownOptions(true, PinState.LOW);
        sensorPinU3.setShutdownOptions(true, PinState.LOW);
        
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
		logger.debug("Reading state of pin U2 (WiringPI 04): {}", sensorPinU2.getState());
		return sensorPinU2.getState() == PinState.HIGH;
	}

	@Override
	public boolean isMovingUp() {
		logger.debug("Reading state of pin U3 (WiringPI 05): {}", sensorPinU3.getState());
		return sensorPinU3.getState() == PinState.HIGH;
	}

	@Override
	public LedState getLed12State() {
		logger.debug("Reading state of led 12 (WiringPI 02): {}", ledPin12.getState().getName());
		return pinStateToLedState(ledPin12);
	}

	@Override
	public LedState getLed13State() {
		logger.debug("Reading state of led 13 (WiringPI 00): {}", ledPin12.getState().getName());
		return pinStateToLedState(ledPin13);
	}

	@Override
	public void setLed12State(LedState state) {		
		logger.debug("Setting state of led 12 (WiringPI 02) to {}", state.toString());
		ledPin12.setState(ledStateToPinState(state));
	}

	@Override
	public void setLed13State(LedState state) {
		logger.debug("Setting state of led 13 (WiringPI 00) to {}", state.toString());
		ledPin13.setState(ledStateToPinState(state));
	}
}
