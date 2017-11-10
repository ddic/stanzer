package de.loosensimnetz.iot.raspi.opcua;

import org.eclipse.milo.opcua.sdk.server.annotations.UaInputArgument;
import org.eclipse.milo.opcua.sdk.server.annotations.UaMethod;
import org.eclipse.milo.opcua.sdk.server.annotations.UaOutputArgument;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.InvocationContext;
import org.eclipse.milo.opcua.sdk.server.util.AnnotationBasedInvocationHandler.Out;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.loosensimnetz.iot.raspi.motor.Motor;
import de.loosensimnetz.iot.raspi.motor.Motor.LedState;

/**
 * Wrapper class for OPC UA method setLedXXState
 * 
 * @author jloosen
 *
 */
public class LedStateMethod implements OpcMethod {
	/**
	 * The motor
	 */
	private final Motor motor;
	/**
	 * One instance of this class for each led
	 */
	private final int ledNumber;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public LedStateMethod(Motor motor, int ledNumber) {
		this.motor = motor;
		this.ledNumber = ledNumber;
	}
	
	/**
	 * The OPC UA method
	 * 
	 * @param context	The OPC UA context
	 * @param x New state of the led (<code>true</code> = ON, <code>false</code> = OFF)
	 * @param xBefore New state of the led (<code>true</code> = ON, <code>false</code> = OFF)
	 */
    @UaMethod
    public void invoke(
        InvocationContext context,

        @UaInputArgument(
            name = "x",
            description = "State of the Led (true = on, false = off")
            boolean x,

        @UaOutputArgument(
            name = "x_before",
            description = "The state of the led before the invocation. True = on, false = off.")
            Out<Boolean> xBefore) {

        logger.debug("Invoking turnLedOn() method of Object '{}'", context.getObjectNode().getBrowseName().getName());
        logger.info("Setting state of led number {} to {}", ledNumber, Boolean.valueOf(x));

        if (ledNumber == 1) {
        	xBefore.set(motor.getLed12State() == LedState.ON ? Boolean.TRUE : Boolean.FALSE);
        	motor.setLed12State(x ? LedState.ON: LedState.OFF);
        }
        
        if (ledNumber == 2) {
        	xBefore.set(motor.getLed13State() == LedState.ON ? Boolean.TRUE : Boolean.FALSE);
        	motor.setLed13State(x ? LedState.ON: LedState.OFF);
        }
    }
}