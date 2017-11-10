package de.loosensimnetz.iot.raspi.motor;

public interface MotorFactory {
	Motor createMotor();
	Motor createMotor(ExpectedTime maxTimeUp, ExpectedTime maxTimeDown);
}
