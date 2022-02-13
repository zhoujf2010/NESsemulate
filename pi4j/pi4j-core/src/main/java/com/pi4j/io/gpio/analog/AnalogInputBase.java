package com.pi4j.io.gpio.analog;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  AnalogInputBase.java
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

/**
 * <p>Abstract AnalogInputBase class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public abstract class AnalogInputBase extends AnalogBase<AnalogInput, AnalogInputConfig, AnalogInputProvider> implements AnalogInput {
    /**
     * <p>Constructor for AnalogInputBase.</p>
     *
     * @param provider a {@link com.pi4j.io.gpio.analog.AnalogInputProvider} object.
     * @param config a {@link com.pi4j.io.gpio.analog.AnalogInputConfig} object.
     */
    public AnalogInputBase(AnalogInputProvider provider, AnalogInputConfig config){
        super(provider, config);
        if(this.id == null) this.id = "AIN-" + config.address();
        if(this.name == null) this.name = "AIN-" + config.address();
    }
}

