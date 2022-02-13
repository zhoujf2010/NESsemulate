package com.pi4j.plugin.pigpio.provider.gpio.digital;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: PLUGIN   :: PIGPIO I/O Providers
 * FILENAME      :  PiGpioDigitalInputProviderImpl.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputProviderBase;
import com.pi4j.library.pigpio.PiGpio;

/**
 * <p>PiGpioDigitalInputProviderImpl class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class PiGpioDigitalInputProviderImpl extends DigitalInputProviderBase implements PiGpioDigitalInputProvider {

    protected final PiGpio piGpio;

    /**
     * <p>Constructor for PiGpioDigitalInputProviderImpl.</p>
     *
     * @param piGpio a {@link com.pi4j.library.pigpio.PiGpio} object.
     */
    public PiGpioDigitalInputProviderImpl(PiGpio piGpio){
        this.id = ID;
        this.name = NAME;
        this.piGpio = piGpio;
    }

    /** {@inheritDoc} */
    @Override
    public DigitalInput create(DigitalInputConfig config) {
        // initialize the PIGPIO library
        if(!piGpio.isInitialized()) piGpio.initialize();

        // create new I/O instance based on I/O config
        return new PiGpioDigitalInput(piGpio, this, config);
    }
}
