/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.IOException;

/**
 * Provides lazy access to a database script.
 */
public interface DbScript {

  /**
   * Gets the name of the script.
   * 
   * @return the name of the script, not null
   */
  String getName();
  
  /**
   * Gets the contents of the script.
   * 
   * @return the contents of the script, not null
   * @throws IOException  if the script cannot be read
   */
  String getScript() throws IOException;
  
}
