package com.pi4j.extension;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  PluginService.java
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
import com.pi4j.platform.Platform;
import com.pi4j.provider.Provider;

/**
 * <p>PluginService interface.</p>
 *
 * @author Robert Savage (<a href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public interface PluginService {
    /**
     * <p>context.</p>
     *
     * @return a {@link com.pi4j.context.Context} object.
     */
    Context context();
    /**
     * <p>register.</p>
     *
     * @param provider a {@link com.pi4j.provider.Provider} object.
     * @return a {@link com.pi4j.extension.PluginService} object.
     */
    PluginService register(Provider ... provider);
    /**
     * <p>register.</p>
     *
     * @param platform a {@link com.pi4j.platform.Platform} object.
     * @return a {@link com.pi4j.extension.PluginService} object.
     */
    PluginService register(Platform... platform);
}
