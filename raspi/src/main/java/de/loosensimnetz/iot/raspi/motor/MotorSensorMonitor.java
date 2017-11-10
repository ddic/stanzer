package de.loosensimnetz.iot.raspi.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.BlinkingLed.BlinkType;
import de.loosensimnetz.iot.raspi.motor.Motor.LedState;
import de.loosensimnetz.iot.raspi.motor.state.MotorStateError;
import de.loosensimnetz.iot.raspi.motor.state.MotorStateMovingDown;
import de.loosensimnetz.iot.raspi.motor.state.MotorStateMovingUp;
import de.loosensimnetz.iot.raspi.motor.state.MotorStateStoppedDown;
import de.loosensimnetz.iot.raspi.motor.state.MotorStateStoppedInitial;

/**
 * Monitor daemon thread class
 * 
 * Monitors the {@link MotorSensor} and calls update() for state updates in
 * regular intervals
 * 
 * @author jloosen
 *
 */
public class MotorSensorMonitor extends Thread {
	private final long updateInterval;
	private final MotorSensor motorSensor;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private BlinkingLed blinkingLed = null;

	/**
	 * Constructor
	 * 
	 * Sets the daemon proerty and descriptive name for the thread.
	 * 
	 * @param updateInterval
	 *            Update interval in milliseconds
	 * @param motorSensor
	 *            The monitored {@link MotorSensor}
	 */
	public MotorSensorMonitor(long updateInterval, MotorSensor motorSensor) {
		super();
		this.updateInterval = updateInterval;
		this.motorSensor = motorSensor;
		this.setDaemon(true);
		this.setName("de.loosensimnetz.iot.raspi.motor.MotorSensorMonitor daemon thread");
		this.setPriority(MIN_PRIORITY);
	}

	/**
	 * MotorSensor thread loop.
	 * 
	 * 1.) Initialize the motor, so the two leds are off. 2.) Call update() method
	 * on the {@link MotorSensor} object in regular intervals 3.) If an error state
	 * is entered, start the BlinkingLed thread and let the leds blink three times
	 * 4.) If an error state is left, interrupt (stop) the BlinkingLed thread. 5.)
	 * 5.) If the motor is moving, turn on the respective led: moving up -> led12
	 * OFF led13 ON moving down -> led12 ON led13 OFF 6.) If the motor is stopped,
	 * turn off the leds
	 * 
	 * approx. every uptadeInterval milliseconds
	 */
	@Override
	public void run() {
		boolean goOn = true;

		logger.info("MotorSensorMonitor started");

		// Initialize leds
		motorSensor.getMotor().setLed12State(LedState.OFF);
		motorSensor.getMotor().setLed13State(LedState.OFF);

		while (goOn && !this.isInterrupted()) {
			logger.debug("Delaying for {} milliseconds", updateInterval);
			
			// Remember the old state
			String oldState = motorSensor.getStateId();
			
			// Sleep for a while
			try {

				Thread.sleep(updateInterval);
			} catch (InterruptedException e) {
				logger.error("MotorSensorMonitor interrupted - exiting", e);
				goOn = false;

				// If we are blinking - interrupt (stop) the thread
				if (blinkingLed != null) {
					blinkingLed.interrupt();
					blinkingLed = null;
				}

				this.interrupt();
			}
			
			// Now update state...
			motorSensor.update(System.currentTimeMillis());
			
			//...and get the new state
			String newState = motorSensor.getStateId();

			// Log message if state has changed
			if (!oldState.equals(newState)) {
				logger.info("Updating state. Old state: {}, new state: {}", oldState, newState);
			}

			// 3.) If an error state is entered, start the BlinkingLed thread and let the
			// leds blink three times
			if (motorSensor.getState() == MotorStateError.instance() && blinkingLed == null) {
				logger.info("Error state - starting to blink.");

				blinkingLed = new BlinkingLed(6, 500L, BlinkType.ALTERNATING, motorSensor);
				blinkingLed.start();
			}

			// 4.) If an error state is left, interrupt (stop) the BlinkingLed thread.
			if (motorSensor.getState() != MotorStateError.instance() && blinkingLed != null) {
				logger.info("Error state left - stopping to blink.");

				blinkingLed.interrupt();
				blinkingLed = null;
			}

			// 5.) If the motor is moving, turn on the respective led:
			// moving up -> led12 OFF led13 ON
			// moving down -> led12 ON led13 OFF
			if (motorSensor.getState() == MotorStateMovingUp.instance()) {
				logger.debug("In MotorStateMovingDown. Turning on led 1.");

				motorSensor.getMotor().setLed12State(LedState.ON);
				motorSensor.getMotor().setLed13State(LedState.OFF);
			}

			if (motorSensor.getState() != MotorStateMovingDown.instance()) {
				logger.debug("In MotorStateMovingDown. Turning on led 2.");

				motorSensor.getMotor().setLed12State(LedState.OFF);
				motorSensor.getMotor().setLed13State(LedState.ON);
			}

			// 6.) If the motor is stopped, turn off the leds
			if (motorSensor.getState() == MotorStateStoppedDown.instance()
					|| motorSensor.getState() == MotorStateStoppedInitial.instance()) {

				logger.debug("In MotorStateStoppedDown or MotorStateStoppedInitial. Turning off leds.");

				motorSensor.getMotor().setLed12State(LedState.OFF);
				motorSensor.getMotor().setLed13State(LedState.OFF);
			}
		}
	}
}
