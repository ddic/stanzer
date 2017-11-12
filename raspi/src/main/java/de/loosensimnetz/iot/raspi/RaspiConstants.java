package de.loosensimnetz.iot.raspi;

public interface RaspiConstants {
	String PROPERTY_MOTORFACTORY = "de.loosensimnetz.iot.raspi.MotorFactory";
	String PROPERTY_TIME_DOWN = "de.loosensimnetz.iot.raspi.ExpectedTimeDown";
	String PROPERTY_TIME_UP = "de.loosensimnetz.iot.raspi.ExpectedTimeUp";
	
	String PROPERTY_VALUE_DEFAULT_MOTORFACTORY = "de.loosensimnetz.iot.raspi.motor.DefaultMotorFactory";
	long PROPERTY_VALUE_TIME_DOWN = 5000L;
	long PROPERTY_VALUE_TIME_UP = 5000L;
}
