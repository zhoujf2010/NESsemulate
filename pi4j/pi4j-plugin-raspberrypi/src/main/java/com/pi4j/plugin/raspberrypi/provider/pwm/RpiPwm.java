package com.pi4j.plugin.raspberrypi.provider.pwm;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: PLUGIN   :: RaspberryPi Platform & Providers
 * FILENAME      :  RpiPwm.java
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

import com.pi4j.io.exception.IOException;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmBase;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmProvider;


/**
 * <p>RpiPwm class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class RpiPwm extends PwmBase implements Pwm {

    /**
     * <p>Constructor for RpiPwm.</p>
     *
     * @param provider a {@link com.pi4j.io.pwm.PwmProvider} object.
     * @param config a {@link com.pi4j.io.pwm.PwmConfig} object.
     */
    public RpiPwm(PwmProvider provider, PwmConfig config){
        super(provider, config);
    }

    /** {@inheritDoc} */
    @Override
    public Pwm on() throws IOException {
        this.onState = true;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Pwm off() throws IOException {
        this.onState = false;
        return this;
    }
}
