package com.pi4j.plugin.pigpio.provider.serial;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: PLUGIN   :: PIGPIO I/O Providers
 * FILENAME      :  PiGpioSerialProvider.java
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

import com.pi4j.io.serial.SerialProvider;
import com.pi4j.library.pigpio.PiGpio;
import com.pi4j.plugin.pigpio.PiGpioPlugin;

/**
 * <p>PiGpioSerialProvider interface.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public interface PiGpioSerialProvider extends SerialProvider {
    /** Constant <code>NAME="PiGpioPlugin.SERIAL_PROVIDER_NAME"</code> */
    String NAME = PiGpioPlugin.SERIAL_PROVIDER_NAME;
    /** Constant <code>ID="PiGpioPlugin.SERIAL_PROVIDER_ID"</code> */
    String ID = PiGpioPlugin.SERIAL_PROVIDER_ID;
    /**
     * <p>newInstance.</p>
     *
     * @param piGpio a {@link com.pi4j.library.pigpio.PiGpio} object.
     * @return a {@link com.pi4j.plugin.pigpio.provider.serial.PiGpioSerialProvider} object.
     */
    static PiGpioSerialProvider newInstance(PiGpio piGpio) {
        return new PiGpioSerialProviderImpl(piGpio);
    }
}
