package de.loosensimnetz.iot.raspi.debug;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;

/**
 * Write a description of class BlinkerTest here.
 * 
 * @author (Joerg Loosen) 
 * @version (a version number or a date)
 */
public class BlinkerTest
{
    /**
     * Entry point from command line
     * 
     * @param  argv   command line parameters
     */
    public static void main(String[] argv)
    {
        try {
            GpioController gpio = GpioFactory.getInstance();
            System.out.println("Start of program.");
            
            // PIN layout: See https://pinout.xyz/pinout/
            GpioPinDigitalOutput ledPin1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.HIGH);
            GpioPinDigitalOutput ledPin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);
            
            ledPin1.setShutdownOptions(true, PinState.LOW);
            ledPin2.setShutdownOptions(true, PinState.LOW);
            
            Thread.sleep(500);

            for (int i=1; i <= 10; i++) {
                System.out.format("Loop count {%d}%n", i);
                
                ledPin1.toggle();
                ledPin2.toggle();

                Thread.sleep(500);
            }
            
            System.out.println("End of program.");
        }
        catch (Throwable t) {
            return;
        }
    }
}
