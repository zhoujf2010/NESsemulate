package com.pi4j.io.i2c;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  I2C.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.context.Context;
import com.pi4j.io.IO;
import com.pi4j.io.IODataReader;
import com.pi4j.io.IODataWriter;


/**
 * I2C I/O Interface for Pi4J I2C Bus/Device Communications
 *
 * @author Robert Savage
 *
 * Based on previous contributions from:
 *        Daniel Sendula,
 *        <a href="http://raspelikan.blogspot.co.at">RasPelikan</a>
 * @version $Id: $Id
 */
public interface I2C extends IO<I2C, I2CConfig, I2CProvider>,
        IODataWriter,
        IODataReader,
        I2CRegisterDataReaderWriter,
        AutoCloseable {

    /**
     * <p>close.</p>
     */
    // Override to remove checked exception declaration
    @Override
    void close();

    /**
     * <p>newConfigBuilder.</p>
     *
     * @param context {@link Context}
     * @return a {@link com.pi4j.io.i2c.I2CConfigBuilder} object.
     */
    static I2CConfigBuilder newConfigBuilder(Context context){
        return I2CConfigBuilder.newInstance(context);
    }

    /**
     * I2C Device Address
     *
     * @return The I2C device address for which this instance is constructed for.
     */
    default int device(){
        return config().device();
    }

    /**
     * I2C Bus Address
     *
     * @return The I2C bus address for which this instance is constructed for.
     */
    default int bus(){
        return config().bus();
    }

    /**
     * I2C Device Communication State is OPEN
     *
     * @return The I2C device communication state
     */
    boolean isOpen();

    /**
     * I2C Bus Address
     *
     * @return The I2C bus address for which this instance is constructed for.
     */
    default int getBus(){
        return bus();
    }

    /**
     * I2C Device Address
     *
     * @return The I2C device address for which this instance is constructed for.
     */
    default int getDevice(){
        return device();
    }

    /**
     * Get an encapsulated interface for reading and writing to a specific I2C device register
     *
     * @param address a int.
     * @return a {@link com.pi4j.io.i2c.I2CRegister} object.
     */
    I2CRegister getRegister(int address);

    /**
     * I2C Device Register
     * Get an encapsulated interface for reading and writing to a specific I2C device register
     *
     * @param address the (16-bit) device register address
     * @return an instance of I2CRegister for the provided register address
     */
    default I2CRegister register(int address){
        return getRegister(address);
    }
}
