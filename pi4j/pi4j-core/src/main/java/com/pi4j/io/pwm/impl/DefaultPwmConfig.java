package com.pi4j.io.pwm.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  DefaultPwmConfig.java
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

import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.impl.IOAddressConfigBase;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmPreset;
import com.pi4j.io.pwm.PwmType;
import com.pi4j.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>DefaultPwmConfig class.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class DefaultPwmConfig
        extends IOAddressConfigBase<PwmConfig>
        implements PwmConfig {

    // private configuration properties
    protected Float dutyCycle = null;
    protected Integer frequency = null;
    protected PwmType pwmType = PwmType.SOFTWARE;
    protected Float shutdownValue = null;
    protected Float initialValue = null;
    protected List<PwmPreset> presets = new ArrayList<>();

    /**
     * PRIVATE CONSTRUCTOR
     */
    private DefaultPwmConfig(){
        super();
    }

    // private configuration properties
    protected PullResistance pullResistance = PullResistance.OFF;

    /**
     * PRIVATE CONSTRUCTOR
     *
     * @param properties a {@link java.util.Map} object.
     * @param presets a {@link java.util.Collection} object.
     */
    protected DefaultPwmConfig(Map<String,String> properties, Collection<PwmPreset> presets){
        this(properties);
        this.presets.addAll(presets);
    }

    /**
     * PRIVATE CONSTRUCTOR
     *
     * @param properties a {@link java.util.Map} object.
     */
    protected DefaultPwmConfig(Map<String,String> properties){
        super(properties);

        // define default property values if any are missing (based on the required address value)
        this.id = StringUtil.setIfNullOrEmpty(this.id, "PWM-" + this.address, true);
        this.name = StringUtil.setIfNullOrEmpty(this.name, "PWM-" + this.address, true);
        this.description = StringUtil.setIfNullOrEmpty(this.description, "PWM-" + this.address, true);

        // load optional pwm duty-cycle from properties
        if(properties.containsKey(DUTY_CYCLE_KEY)){
            this.dutyCycle = Float.parseFloat(properties.get(DUTY_CYCLE_KEY));
        }

        // load optional pwm frequency from properties
        if(properties.containsKey(FREQUENCY_KEY)){
            this.frequency = Integer.parseInt(properties.get(FREQUENCY_KEY));
        }

        // load optional pwm type from properties
        if(properties.containsKey(PWM_TYPE_KEY)){
            this.pwmType = PwmType.parse(properties.get(PWM_TYPE_KEY));
        }

        // load initial value property
        if(properties.containsKey(INITIAL_VALUE_KEY)){
            this.initialValue = Float.parseFloat(properties.get(INITIAL_VALUE_KEY));
        }

        // load shutdown value property
        if(properties.containsKey(SHUTDOWN_VALUE_KEY)){
            this.shutdownValue = Float.parseFloat(properties.get(SHUTDOWN_VALUE_KEY));
        }

        // bounds checking
        if(this.dutyCycle != null && this.dutyCycle > 100)
            this.dutyCycle = 100f;

        // bounds checking
        if(this.dutyCycle != null && this.dutyCycle < 0)
            this.dutyCycle = 0f;
    }

    /** {@inheritDoc} */
    @Override
    public Float dutyCycle() {
        return this.dutyCycle;
    }

    /** {@inheritDoc} */
    @Override
    public Integer frequency() {
        return this.frequency;
    }

    /** {@inheritDoc} */
    @Override
    public PwmType pwmType() {
        return this.pwmType;
    }

    /** {@inheritDoc} */
    @Override
    public Float shutdownValue(){
        return this.shutdownValue;
    }

    /** {@inheritDoc} */
    @Override
    public PwmConfig shutdownValue(Number dutyCycle){

        // bounds check the duty-cycle value
        float dc = dutyCycle.floatValue();
        if(dc < 0) dc = 0;
        if(dc > 100) dc = 100;

        this.shutdownValue = dc;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Float initialValue() {
        return this.initialValue;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<PwmPreset> presets(){
        return this.presets;
    }
}
