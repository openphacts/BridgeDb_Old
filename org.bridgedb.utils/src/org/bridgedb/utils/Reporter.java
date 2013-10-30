// BridgeDb,
// An abstraction layer for identifier mapping services, both local and online.
//
// Copyright 2006-2009 BridgeDb developers
// Copyright 2012-2013  Christian Y. A. Brenninkmeijer
// Copyright 2012-2013  OpenPhacts
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.bridgedb.utils;

import org.apache.log4j.Logger;

/**
 * Util functions that allows messages to be output.
 * <p>
 * Allows the output format to be changed in one place so changing everywhere.
 * <p>
 * All other System.out calls can then be considered debug commands that should not have stayed in.
 * 
 * @author Christian
 */
public class Reporter {
    
    static final Logger logger = Logger.getLogger(Reporter.class);
   
    /**
     * Messages that should always goto the System out stream.
     * This method just for the ease of searching for System,out lines added for debugging.
     * @param message 
     */
    
    public static void println(String message){
        if (ConfigReader.loggerSetup){
            logger.info(message);
        }
        System.out.println(message);
    }

    public static void warn(String message){
        if (ConfigReader.loggerSetup){
            logger.warn(message);
        }
        System.out.println(message);
    }
    
    public static void error(String message){
        if (ConfigReader.loggerSetup){
            logger.error(message);
        }
        System.err.println(message);
    }

    public static void error(String message, Exception ex){
        if (ConfigReader.loggerSetup){
            logger.error(message, ex);
        }
        System.err.println(message);
        System.err.println(ex);
    }
}
