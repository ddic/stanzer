package de.loosensimnetz.iot.raspi.motor;

public class DefaultMotorFactory implements MotorFactory {

	@Override
	public Motor createMotor() {
		return new RaspiMotor();
	}

	@Override
	public Motor createMotor(ExpectedTime maxTimeUp, ExpectedTime maxTimeDown) {
		return new RaspiMotor(maxTimeUp, maxTimeDown);
	}

}
