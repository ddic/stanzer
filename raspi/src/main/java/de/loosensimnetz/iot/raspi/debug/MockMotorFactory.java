package de.loosensimnetz.iot.raspi.debug;

import de.loosensimnetz.iot.raspi.motor.ExpectedTime;
import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.MotorFactory;

public class MockMotorFactory implements MotorFactory {

	@Override
	public Motor createMotor() {
		return new MockMotor();
	}

	@Override
	public Motor createMotor(ExpectedTime maxTimeUp, ExpectedTime maxTimeDown) {
		return new MockMotor(maxTimeUp, maxTimeDown);
	}

}
