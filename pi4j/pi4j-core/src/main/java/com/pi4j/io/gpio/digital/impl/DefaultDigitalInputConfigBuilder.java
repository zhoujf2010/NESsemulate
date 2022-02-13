package com.pi4j.io.gpio.digital.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  DefaultDigitalInputConfigBuilder.java
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
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalInputConfigBuilder;
import com.pi4j.io.gpio.digital.PullResistance;

import java.util.concurrent.TimeUnit;

/**
 * <p>DefaultDigitalInputConfigBuilder class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class DefaultDigitalInputConfigBuilder
        extends DigitalConfigBuilderBase<DigitalInputConfigBuilder, DigitalInputConfig>
        implements DigitalInputConfigBuilder {

    /**
     * PRIVATE CONSTRUCTOR
     */
    protected DefaultDigitalInputConfigBuilder(Context context){
        super(context);
    }

    /**
     * <p>newInstance.</p>
     *
     * @return a {@link com.pi4j.io.gpio.digital.DigitalInputConfigBuilder} object.
     */
    public static DigitalInputConfigBuilder newInstance(Context context) {
        return new DefaultDigitalInputConfigBuilder(context);
    }

    /** {@inheritDoc} */
    @Override
    public DigitalInputConfig build() {
        DigitalInputConfig config = new DefaultDigitalInputConfig(getResolvedProperties());
        return config;
    }

    /** {@inheritDoc} */
    @Override
    public DigitalInputConfigBuilder pull(PullResistance value) {
        this.properties.put(DigitalInputConfig.PULL_RESISTANCE_KEY, value.toString());
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public DigitalInputConfigBuilder debounce(Long microseconds) {
        if(microseconds != null) {
            this.properties.put(DigitalInputConfig.DEBOUNCE_RESISTANCE_KEY, microseconds.toString());
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public DigitalInputConfigBuilder debounce(Long interval, TimeUnit units) {
        return debounce(units.toMicros(interval));
    }
}
