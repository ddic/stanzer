/**
 * 
 */
package de.loosensimnetz.iot.raspi.motor.state;

import static org.junit.Assert.*;

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
		motor.setMovingDown(true);		
		
		// Update sensor readings after runtime of 1000 ms
		sensor.update(1000L);
		
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
	}

	@Test
	public void testTransitionFromMovingDownContinued() {
		logger.info("*************** testTransitionFromMovingDownContinued");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		motor.setExpectedTimeDown(new ExpectedTime(5000L, 0L));
		
		
		// Begin moving downward
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		sensor.update(1000L);
		assertTrue("State should be MotorMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 4000 ms
		sensor.update(4000L);
	
		assertTrue("State should still be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
	}
	
	@Test
	public void testTransitionFromMovingDownToStoppedTooEarly() {
		logger.info("*************** testTransitionFromMovingDownTooEarly");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		sensor.update(1000L);
		assertTrue("State should be MotorMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 3000 ms
		motor.setMovingDown(false);
		sensor.update(3000L);

		assertTrue("State should be MotorStateError", sensor.getState() instanceof MotorStateError);
	}
	
	@Test
	public void testTransitionFromMovingDownToStoppedTooLate() {
		logger.info("*************** testTransitionFromMovingDownTooLate");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		sensor.update(1000L);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 7000 ms
		motor.setMovingDown(false);
		sensor.update(7000L);
	
		assertTrue("State should be Error", sensor.getState() instanceof MotorStateError);
	}
	
	@Test
	public void testTransitionFromMovingDownToStopped() {
		logger.info("*************** testTransitionFromMovingDownToStopped");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		sensor.update(1000L);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 6000 ms
		motor.setMovingDown(false);
		sensor.update(6000L);
	
		assertTrue("State should be MotorStateStoppedDown", sensor.getState() instanceof MotorStateStoppedDown);
	}
	
	@Test
	public void testTransitionFromMovingDownToMovingUpSkippingStopped() {
		logger.info("*************** testTransitionFromMovingDownToMovingUpSkippingStopped");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		sensor.update(1000L);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 6000 ms
		motor.setMovingDown(false);
		motor.setMovingUp(true);
		sensor.update(6000L);
	
		assertTrue("State should be MotorStateStoppedDown", sensor.getState() instanceof MotorStateMovingUp);
	}
	
	@Test
	public void testTransitionFromStoppedToMovingUp() {
		logger.info("*************** testTransitionFromStoppedToMovingUp");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		sensor.update(1000L);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 6000 ms
		motor.setMovingDown(false);
		sensor.update(6000L);
	
		assertTrue("State should be MotorStateStoppedDown", sensor.getState() instanceof MotorStateStoppedDown);
		
		// Update again after runtime of 7000 ms
		motor.setMovingUp(true);
		sensor.update(7000L);
	
		assertTrue("State should be MotorStateMovingUp", sensor.getState() instanceof MotorStateMovingUp);
	}
	
	@Test
	public void testTransitionFromStoppedToMovingUpTooEarly() {
		logger.info("*************** testTransitionFromStoppedToMovingUpTooEarly");
		
		MockMotor motor = new MockMotor();		
		MotorSensor sensor = new MotorSensor(motor, 0L);
		
		
		// Begin moving downward
		motor.setMovingDown(true);			
		// Update sensor readings after runtime of 1000 ms
		sensor.update(1000L);
		assertTrue("State should be MotorStateMovingDown", sensor.getState() instanceof MotorStateMovingDown);
		
		// Update again after runtime of 6000 ms
		motor.setMovingDown(false);
		sensor.update(6000L);
	
		assertTrue("State should be MotorStateStoppedDown", sensor.getState() instanceof MotorStateStoppedDown);
		
		// Update again after runtime of 7000 ms
		motor.setMovingUp(true);
		sensor.update(7000L);
	
		assertTrue("State should be MotorStateMovingUp", sensor.getState() instanceof MotorStateMovingUp);
	}
}
