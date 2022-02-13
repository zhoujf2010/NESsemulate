package com.pi4j.exception;

/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  Pi4JException.java
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
 * <p>
 * Base exception class for Pi4J exceptions.
 * </p>
 *
 * @see <a href="http://www.pi4j.com/">http://www.pi4j.com/</a>
 * @author Robert Savage (<a
 *         href="http://www.savagehomeautomation.com">http://www.savagehomeautomation.com</a>)
 * @version $Id: $Id
 */
public class Pi4JException extends RuntimeException {

    /**
     * Default Constructor
     *
     * @param message error message
     */
    public Pi4JException(String message){
        super(message);
    }

    /**
     * <p>Constructor for Pi4JException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public Pi4JException(Throwable cause){
        super(cause);
    }

    /**
     * <p>Constructor for Pi4JException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public Pi4JException(String message, Throwable cause){
        super(message,cause);
    }
}
