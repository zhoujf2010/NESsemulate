package com.pi4j.io.pwm.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  DefaultPwmConfigBuilder.java
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
import com.pi4j.io.impl.IOAddressConfigBuilderBase;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmConfigBuilder;
import com.pi4j.io.pwm.PwmPreset;
import com.pi4j.io.pwm.PwmType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>DefaultPwmConfigBuilder class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class DefaultPwmConfigBuilder
        extends IOAddressConfigBuilderBase<PwmConfigBuilder, PwmConfig>
        implements PwmConfigBuilder {

    protected List<PwmPreset> presets = new ArrayList<>();

    /**
     * PRIVATE CONSTRUCTOR
     */
    protected DefaultPwmConfigBuilder(Context context){
        super(context);
    }

    /**
     * <p>newInstance.</p>
     *
     * @return a {@link com.pi4j.io.pwm.PwmConfigBuilder} object.
     */
    public static PwmConfigBuilder newInstance(Context context) {
        return new DefaultPwmConfigBuilder(context);
    }

    /** {@inheritDoc} */
    @Override
    public PwmConfigBuilder frequency(Integer frequency) {
        this.properties.put(PwmConfig.FREQUENCY_KEY, frequency.toString());
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PwmConfigBuilder dutyCycle(Number dutyCycle) {
        // bounds check the duty-cycle value
        float dc = dutyCycle.floatValue();
        if(dc < 0) dc = 0;
        if(dc > 100) dc = 100;

        this.properties.put(PwmConfig.DUTY_CYCLE_KEY, Float.toString(dc));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PwmConfigBuilder pwmType(PwmType pwmType) {
        this.properties.put(PwmConfig.PWM_TYPE_KEY, pwmType.toString());
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PwmConfigBuilder shutdown(Number dutyCycle) {
        // bounds check the duty-cycle value
        float dc = dutyCycle.floatValue();
        if(dc < 0) dc = 0;
        if(dc > 100) dc = 100;

        this.properties.put(PwmConfig.SHUTDOWN_VALUE_KEY, Float.toString(dc));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PwmConfigBuilder initial(Number dutyCycle) {

        // bounds check the duty-cycle value
        float dc = dutyCycle.floatValue();
        if(dc < 0) dc = 0;
        if(dc > 100) dc = 100;

        this.properties.put(PwmConfig.INITIAL_VALUE_KEY, Float.toString(dc));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PwmConfigBuilder preset(PwmPreset ... preset){
        for(PwmPreset p : preset) {
            this.presets.add(p);
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PwmConfig build() {
        PwmConfig config = new DefaultPwmConfig(getResolvedProperties(), this.presets);
        return config;
    }
}
