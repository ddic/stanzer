package de.loosensimnetz.iot.raspi;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.RaspiMotor;

public class DefaultMotorFactory implements MotorFactory {

	@Override
	public Motor createMotor() {
		return new RaspiMotor();
	}

}
