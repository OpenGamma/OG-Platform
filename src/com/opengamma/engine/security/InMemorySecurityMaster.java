/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

import java.util.Collection;


/**
 * A simple purely in-memory implementation of the {@link SecurityMaster}
 * interface.
 * This class is primarily useful in testing scenarios, or when operating
 * as a cache on top of a slower {@link SecurityMaster} implementation.
 *
 * @author kirk
 */
public class InMemorySecurityMaster implements SecurityMaster {

  @Override
  public Collection<Security> getSecurities(SecurityKey secKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Security getSecurity(SecurityKey secKey) {
    // TODO Auto-generated method stub
    return null;
  }

}
