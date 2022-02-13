package com.pi4j.io.gpio.digital.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  DefaultDigitalInputConfig.java
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

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.impl.IOAddressConfigBase;
import com.pi4j.util.StringUtil;

import java.util.Map;

/**
 * <p>DefaultDigitalInputConfig class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class DefaultDigitalInputConfig
        extends IOAddressConfigBase<DigitalInputConfig>
        implements DigitalInputConfig {

    /**
     * PRIVATE CONSTRUCTOR
     */
    private DefaultDigitalInputConfig(){
        super();
    }

    // private configuration properties
    protected PullResistance pullResistance = PullResistance.OFF;
    protected Long debounce = DigitalInput.DEFAULT_DEBOUNCE;

    /**
     * PRIVATE CONSTRUCTOR
     *
     * @param properties a {@link java.util.Map} object.
     */
    protected DefaultDigitalInputConfig(Map<String,String> properties){
        super(properties);

        // define default property values if any are missing (based on the required address value)
        this.id = StringUtil.setIfNullOrEmpty(this.id, "DIN-" + this.address, true);
        this.name = StringUtil.setIfNullOrEmpty(this.name, "DIN-" + this.address, true);
        this.description = StringUtil.setIfNullOrEmpty(this.description, "DIN-" + this.address, true);

        // load optional pull resistance from properties
        if(properties.containsKey(PULL_RESISTANCE_KEY)){
            this.pullResistance = PullResistance.parse(properties.get(PULL_RESISTANCE_KEY));
        }

        // load optional pull resistance from properties
        if(properties.containsKey(DEBOUNCE_RESISTANCE_KEY)){
            this.debounce = Long.parseLong(properties.get(DEBOUNCE_RESISTANCE_KEY));
        }
    }

    /** {@inheritDoc} */
    @Override
    public PullResistance pull() {
        return this.pullResistance;
    }

    /** {@inheritDoc} */
    @Override
    public Long debounce() { return this.debounce; }
}
