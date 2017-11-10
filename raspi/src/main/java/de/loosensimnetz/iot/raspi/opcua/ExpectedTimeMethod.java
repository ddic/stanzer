package de.loosensimnetz.iot.raspi.opcua;

import org.eclipse.milo.opcua.sdk.server.annotations.UaInputArgument;
import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod;
import org.eclipse.milo.opcua.sdk.server.annotations.UaOutputArgument;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.InvocationContext;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.ExpectedTime;
import de.loosensimnetz.iot.raspi.motor.Motor;

/**
 * Wrapper class for OPC UA methods setExpectedTimeUp, setExpectedTimeDown and setExpectedTimeStoppedDown
 * 
 * @author jloosen
 *
 */
public class ExpectedTimeMethod implements OpcMethod {
	public enum TimeType {
		TIME_DOWN, TIME_UP, TIME_STOPPED_DOWN
	};

	/**
	 * The motor
	 */
	private final Motor motor;
	/**
	 * One instance of this wrapper class for each TimeType
	 */
	private final TimeType timeType;
	
	private static final Logger logger = LoggerFactory.getLogger(ExpectedTimeMethod.class);

	public ExpectedTimeMethod(Motor motor, TimeType timeType) {
		this.motor = motor;
		this.timeType = timeType;
	}

	/**
	 * The OPC UA method
	 * 
	 * @param context	The OPC UA context
	 * @param expectedTime	New value in milliseconds for expected time
	 * @param tolerance New value in milliseconds for tolerance
	 * @param expectedTimeBefore Old value in milliseconds for expected time
	 * @param toleranceBefore Old value in milliseconds for tolerance
	 */
	@UaMethod
	public void invoke(InvocationContext context,

			@UaInputArgument(name = "expectedTime", description = "Expected time in milliseconds") long expectedTime,

			@UaInputArgument(name = "tolerance", description = "Tolerance in milliseconds") long tolerance,

			@UaOutputArgument(name = "expectedTime_before", description = "The expected time before the invocation in milliseconds.") Out<Long> expectedTimeBefore,

			@UaOutputArgument(name = "tolerance_before", description = "The tolerance before the invocation in milliseconds.") Out<Long> toleranceBefore) {

		String methodName = "setExpected";

		switch (timeType) {
		case TIME_DOWN:

			methodName += "TimeDown";
			ExpectedTime expectedTimeDown = motor.getExpectedTimeDown();

			expectedTimeBefore.set(expectedTimeDown.getExpectedTime());
			toleranceBefore.set(expectedTimeDown.getTolerance());

			motor.setExpectedTimeDown(new ExpectedTime(expectedTime, tolerance));
			break;
			
		case TIME_STOPPED_DOWN:

			methodName += "StoppedDown";
			ExpectedTime expectedTimeStoppedDown = motor.getExpectedTimeDown();

			expectedTimeBefore.set(expectedTimeStoppedDown.getExpectedTime());
			toleranceBefore.set(expectedTimeStoppedDown.getTolerance());

			motor.setExpectedTimeStoppedDown(new ExpectedTime(expectedTime, tolerance));
			break;
			
		case TIME_UP:

			methodName += "TimeUp";
			ExpectedTime expectedTimeUp = motor.getExpectedTimeDown();

			expectedTimeBefore.set(expectedTimeUp.getExpectedTime());
			toleranceBefore.set(expectedTimeUp.getTolerance());

			motor.setExpectedTimeUp(new ExpectedTime(expectedTime, tolerance));
			break;
		}

		logger.debug("Invoking {}() method of Object '{}'", methodName,
				context.getObjectNode().getBrowseName().getName());
		logger.info("Setting expected time value for {} to {}, tolerance to {}.", timeType, expectedTime, tolerance);
	}
}