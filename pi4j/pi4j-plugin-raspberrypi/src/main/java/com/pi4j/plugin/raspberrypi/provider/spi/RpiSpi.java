package com.pi4j.plugin.raspberrypi.provider.spi;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: PLUGIN   :: RaspberryPi Platform & Providers
 * FILENAME      :  RpiSpi.java
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

import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBase;
import com.pi4j.io.spi.SpiConfig;
import com.pi4j.io.spi.SpiProvider;

/**
 * <p>RpiSpi class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class RpiSpi extends SpiBase implements Spi {

    /**
     * <p>Constructor for RpiSpi.</p>
     *
     * @param provider a {@link com.pi4j.io.spi.SpiProvider} object.
     * @param config a {@link com.pi4j.io.spi.SpiConfig} object.
     */
    public RpiSpi(SpiProvider provider, SpiConfig config){
        super(provider, config);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOpen() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void open() {
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
    }

    /** {@inheritDoc} */
    @Override
    public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int numberOfBytes) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int read() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int read(byte[] buffer, int offset, int length) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int write(byte b) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int write(byte[] data, int offset, int length) {
        return 0;
    }
}
