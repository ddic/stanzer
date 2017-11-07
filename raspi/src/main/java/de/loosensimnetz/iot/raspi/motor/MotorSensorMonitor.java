package de.loosensimnetz.iot.raspi.motor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor daemon thread class
 * 
 * Monitors the {@link MotorSensor} and calls update() for state updates in regular intervals
 * 
 * @author jloosen
 *
 */
public class MotorSensorMonitor extends Thread {
	private final long updateInterval;
	private final MotorSensor sensor;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor
	 * 
	 * Sets the daemon proerty and descriptive name for the thread.
	 * 
	 * @param updateInterval Update interval in milliseconds
	 * @param sensor The monitored {@link MotorSensor}
	 */
	public MotorSensorMonitor(long updateInterval, MotorSensor sensor) {
		super();
		this.updateInterval = updateInterval;
		this.sensor = sensor;
		this.setDaemon(true);
		this.setName("de.loosensimnetz.iot.raspi.motor.MotorSensorMonitor daemon thread");
		this.setPriority(MIN_PRIORITY);
	}

	/**
	 * Call update() method on the {@link MotorSensor} object in regular intervals approx.
	 * every uptadeInterval milliseconds
	 */
	@Override
	public void run() {
		boolean goOn = true;
		
		logger.info("MotorSensorMonitor started");

		while (goOn && !this.isInterrupted()) {
			logger.info("Delaying for {} milliseconds");

			try {
				String oldState = sensor.getStateId();
				Thread.sleep(updateInterval);
				sensor.update(System.currentTimeMillis());
				String newState = sensor.getStateId();
								
				logger.info("Updating state. Old state: {}, new state: {}", oldState, newState);
			} catch (InterruptedException e) {
				logger.error("MotorSensorMonitor interrupted - exiting", e);
				goOn = false;
				this.interrupt();
			}
		}
	}

}
