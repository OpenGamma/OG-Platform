/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.marketdata;

import java.util.Collection;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;

/**
 * Mock security loader to get the example engine server up and running
 * 
 * For fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please contact sales@opengamma.com
 */
public class MockSecurityLoader implements SecurityLoader {
  
  private static final String MESSAGE = "This is a placeholder security loader." +
      "\nFor fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters," +
      "\nPlease contact sales@opengamma.com.";
  
  private SecurityMaster _securityMaster;
    
  @Override
  public Map<ExternalIdBundle, UniqueId> loadSecurity(Collection<ExternalIdBundle> identifiers) {
    System.out.println(MESSAGE);
    throw new OpenGammaRuntimeException(MESSAGE);
  }

  @Override
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

}
