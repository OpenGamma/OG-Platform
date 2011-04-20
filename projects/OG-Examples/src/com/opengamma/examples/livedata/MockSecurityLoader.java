/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.livedata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;

/**
 * Mock security loader to get the example engine server up and running
 * 
 * For fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please contact sales@opengamma.com
 */
public class MockSecurityLoader implements SecurityLoader {
  
  private SecurityMaster _securityMaster;
    
  public MockSecurityLoader(SecurityMaster securityMaster) {
    _securityMaster = securityMaster;
  }

  @Override
  public Map<IdentifierBundle, UniqueIdentifier> loadSecurity(Collection<IdentifierBundle> identifiers) {
    System.out.println("This is a placeholder security loader. For fully supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, " +
      " please contact sales@opengamma.com.");
    return Collections.emptyMap();
  }

  @Override
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

}
