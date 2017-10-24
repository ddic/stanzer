package de.loosensimnetz.iot.raspi.motor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class RaspiMotor implements Motor {
	
	private final GpioController gpio;
	private final GpioPinDigitalOutput ledPin1;
	private final GpioPinDigitalOutput ledPin2;

    public RaspiMotor() {
    	gpio = GpioFactory.getInstance();
    	
    	ledPin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.HIGH);
        ledPin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);
        
        ledPin1.setShutdownOptions(true, PinState.LOW);
        ledPin2.setShutdownOptions(true, PinState.LOW);
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
		if (ledPin1.getState() == PinState.HIGH) {
			return LedState.ON;
		}
		else {
			return LedState.OFF;
		}
	}

	@Override
	public LedState getLed2State() {
		if (ledPin2.getState() == PinState.HIGH) {
			return LedState.ON;
		}
		else {
			return LedState.OFF;
		}
	}

	@Override
	public void setLed1State(LedState state) {
		if (state == LedState.ON) {
			ledPin1.setState(PinState.HIGH);
		}
		else {
			ledPin1.setState(PinState.LOW);
		}
	}

	@Override
	public void setLed2State(LedState state) {
		if (state == LedState.ON) {
			ledPin2.setState(PinState.HIGH);
		}
		else {
			ledPin2.setState(PinState.LOW);
		}
	}

	
}
