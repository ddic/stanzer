package de.loosensimnetz.iot.raspi;

import de.loosensimnetz.iot.raspi.motor.Motor;

public interface MotorFactory {
	Motor createMotor();
}
