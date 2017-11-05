package de.loosensimnetz.iot.raspi.motor;

public class DefaultMotorFactory implements MotorFactory {

	@Override
	public Motor createMotor() {
		return new RaspiMotor();
	}

}
