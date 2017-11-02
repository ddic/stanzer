package de.loosensimnetz.iot.raspi.motor;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import de.loosensimnetz.iot.raspi.motor.Motor.LedState;

/**
 * Helper class to convert from internal LedState to external GPIO PinState
 * @author jloosen
 *
 */
public class LedStateConverter {
	/**
	 * Convert GPIO pinstate to internal LedState
	 * 
	 * @param pin GPIO pinstate
	 * @return Internal LedState
	 */
	public static LedState pinStateToLedState(GpioPinDigitalOutput pin) {
		if (pin.getState() == PinState.HIGH) {
			return LedState.ON;
		}
		else {
			return LedState.OFF;
		}
	}

	/**
	 * Convert internal LedState to GPIO pinstate
	 * 
	 * @param state Internal LedState
	 * @return GPIO pinstate
	 */
	public static PinState ledStateToPinState(LedState state) {
		if (state == LedState.ON) {
			return PinState.HIGH;
		}
		else {
			return PinState.LOW;
		}
	}
}
