package de.loosensimnetz.iot.raspi.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor.LedState;

/**
 * Daemon thread to make the leds blink a few times.
 * 
 * Two modes: ALTERNATING means led1 = ON, led2 = OFF, then toggle SIMULTANEAOUS
 * means led1 = ON, led2 = ON, then toggle
 * 
 * @author jloosen
 *
 */
public class BlinkingLed extends Thread {
	private final int numBlink;
	private final long delay;
	private final BlinkType blinkType;
	private final MotorSensor motorSensor;

	private LedState ledState12, ledState13;

	private final static Logger logger = LoggerFactory.getLogger(BlinkingLed.class);

	/**
	 * Blinking mode - alternating vs. simultaneous mode
	 * 
	 * @author jloosen
	 *
	 */
	public enum BlinkType {
		ALTERNATING, SIMULTANEOUS
	}

	/**
	 * Constructor
	 * 
	 * @param numBlink
	 *            Number of times the leds blink. Zero means blinking until thread
	 *            is interrupted. Must be greater than or equal to zero.
	 * @param delay
	 *            Delay in milliseconds between the toggling. Must be greater than
	 *            or equal to zero.
	 * @param blinkType
	 *            Mode = ALTERNATING or SIMULTANEOUS
	 * @param motorSensor
	 *            The motor sensor
	 */
	public BlinkingLed(int numBlink, long delay, BlinkType blinkType, MotorSensor motorSensor) {
		super();
		this.numBlink = numBlink;
		this.delay = delay;
		this.blinkType = blinkType;
		this.motorSensor = motorSensor;
		this.setDaemon(true);
		this.setName("BlinkingLed daemon thread");

		if (numBlink < 0)
			throw new IllegalArgumentException("Parameter numBlink must be greater than or eaqual to zero!");
		if (delay < 0)
			throw new IllegalArgumentException("Parameter numBlink must be greater than or eaqual to zero!");
	}

	/**
	 * Initialize the leds according to the BlinkingMode. Then toggle the leds
	 * according to the values provided.
	 * 
	 */
	@Override
	public void run() {
		logger.info("Starting to blink {} times. Mode: {}, delay: {}", numBlink, blinkType, delay);
		int round = 1;
		boolean goOn = true;

		switch (blinkType) {
		case ALTERNATING:
			initAlternating();
			break;
		case SIMULTANEOUS:
			initSimultaneous();
			break;
		}

		while (goOn && !isInterrupted()) {
			if (numBlink > 0) {
				if (round++ >= numBlink)
					goOn = false;
			}

			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// interrupted while sleeping
				logger.info("Thread was interrupted. Exiting.");

				goOn = false;
				interrupt();
			}

			ledState12 = toggle(ledState12);
			ledState13 = toggle(ledState13);

			motorSensor.getMotor().setLed12State(ledState12);
			motorSensor.getMotor().setLed13State(ledState13);
		}
	}

	/**
	 * ON --> OFF and OFF --> ON
	 * 
	 * @param ledState
	 *            Input led state
	 * @return Toggled led state
	 */
	private LedState toggle(LedState ledState) {
		return (ledState == LedState.ON) ? LedState.OFF : LedState.ON;
	}

	/**
	 * Initialize with both leds on
	 */
	private void initSimultaneous() {
		ledState12 = LedState.ON;
		ledState13 = LedState.ON;
	}

	/**
	 * Initialize with one led on and the other off
	 */
	private void initAlternating() {
		ledState12 = LedState.ON;
		ledState13 = LedState.OFF;
	};

}
