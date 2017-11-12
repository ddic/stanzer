/**
 * 
 */
package de.loosensimnetz.iot.raspi.motor.state;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.debug.MockMotor;
import de.loosensimnetz.iot.raspi.motor.ExpectedTime;
import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorSensor;

/**
 * @author jloosen
 *
 */
public class MotorSensorTest {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private long currentTime = 0L;
	
	private void start() {
		currentTime = 0L;
	}
	
	private void waitFor(long time) {
		currentTime += time;
	}

	/**
	 * Test method for {@link de.loosensimnetz.iot.raspi.motor.state.MotorState#update(de.loosensimnetz.iot.raspi.motor.Motor, long)}.
	 */
	@Test
	public void testInitialIsStateMotorStateStoppedInitial() {
		logger.info("*************** testInitialIsStateMotorStateStoppedInitial");
		
		Motor motor = new MockMotor();
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		assertTrue("Initial state should be MotorStateStoppedInitial", sensor.getState() instanceof MotorStateStoppedInitial);
	}
	
	@Test
	public void testTransitionFromInitialToMovingDown() {
		logger.info("*************** testTransitionFromInitialToMovingDown");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);		
		
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
	}

	@Test
	public void testTransitionFromMovingDownContinued() {
		logger.info("*************** testTransitionFromMovingDownContinued");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		motor.setExpectedTimeDown(new ExpectedTime(5000L, 0L));
		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after 3000 ms
		waitFor(3000L);
		sensor.update(currentTime);
	
		assertTrue("State should still be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
	}
	
	@Test
	public void testTransitionFromMovingDownToStoppedTooEarly() {
		logger.info("*************** testTransitionFromMovingDownTooEarly");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after 2000 ms
		waitFor(2000L);
		motor.setMovingDown(false);
		sensor.update(currentTime);

		assertTrue("State should be MotorStateError", sensor.getState() instanceof MotorStateError);
	}
	
	@Test
	public void testTransitionFromMovingDownToStoppedTooLate() {
		logger.info("*************** testTransitionFromMovingDownTooLate");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 7000 ms
		waitFor(7000L);
		motor.setMovingDown(false);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateError", sensor.getState() instanceof MotorStateError);
	}
	
	@Test
	public void testTransitionFromMovingDownToStopped() {
		logger.info("*************** testTransitionFromMovingDownToStopped");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);
		
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 5000 ms
		waitFor(5000L);
		motor.setMovingDown(false);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateStoppedDown", sensor.getState() instanceof MotorStateStoppedDown);
	}
	
	@Test
	public void testTransitionFromMovingDownToMovingUpSkippingStopped() {
		logger.info("*************** testTransitionFromMovingDownToMovingUpSkippingStopped");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 5000 ms
		waitFor (5000L);
		motor.setMovingDown(false);
		motor.setMovingUp(true);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateMovingUp", sensor.getState() instanceof MotorStateMovingUp);
	}
	
	@Test
	public void testTransitionFromStoppedToMovingUp() {
		logger.info("*************** testTransitionFromStoppedToMovingUp");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 5000 ms
		waitFor(5000L);
		motor.setMovingDown(false);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateStoppedDown", sensor.getState() instanceof MotorStateStoppedDown);
		
		// Update again after runtime of 1000 ms
		waitFor(1000L);
		motor.setMovingUp(true);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateMovingUp", sensor.getState() instanceof MotorStateMovingUp);
	}
	
	@Test
	public void testTransitionFromMovingDownToMovingUpSkippingStoppedTooEarly() {
		logger.info("*************** testTransitionFromMovingDownToMovingUpSkippingStoppedTooEarly");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 4000 ms
		waitFor (4000L);
		motor.setMovingDown(false);
		motor.setMovingUp(true);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateError", sensor.getState() instanceof MotorStateError);
	}
	
	@Test
	@Ignore		// Taking too long is no longer an error
	public void testTransitionFromMovingDownToMovingUpSkippingStoppedTooLate() {
		logger.info("*************** testTransitionFromMovingDownToMovingUpSkippingStoppedTooLate");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 6000 ms
		waitFor (6000L);
		motor.setMovingDown(false);
		motor.setMovingUp(true);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateError", sensor.getState() instanceof MotorStateError);
	}
	
	@Test
	public void testTransitionFromMovingUpToStopped() {
		logger.info("*************** testTransitionFromMovingUpToStopped");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		start();
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		waitFor(1000L);
		sensor.update(currentTime);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 5000 ms
		waitFor(5000L);
		motor.setMovingDown(false);
		motor.setMovingUp(true);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateMovingUp", sensor.getState() instanceof MotorStateMovingUp);
		
		// Update again after runtime of 5000 ms
		waitFor(5000L);
		motor.setMovingDown(false);
		motor.setMovingUp(false);
		sensor.update(currentTime);
	
		assertTrue("State should be MotorStateStoppedInitial", sensor.getState() instanceof MotorStateStoppedInitial);
	}
}
