package de.loosensimnetz.iot.raspi.debug;

import de.loosensimnetz.iot.raspi.MotorFactory;
import de.loosensimnetz.iot.raspi.motor.Motor;

public class MockMotorFactory implements MotorFactory {

	@Override
	public Motor createMotor() {
		return new MockMotor();
	}

}
