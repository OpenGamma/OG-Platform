/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.master.security.impl.AbstractSecurityLoader;

/**
 * Mock security loader to get the example engine server up and running
 * 
 * For fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please contact sales@opengamma.com
 */
public class MockSecurityLoader extends AbstractSecurityLoader {
  
  private static final String MESSAGE = "This is a placeholder security loader." +
      "\nFor fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters," +
      "\nPlease contact sales@opengamma.com.";
  
  @Override
  protected SecurityLoaderResult doBulkLoad(SecurityLoaderRequest request) {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

}
