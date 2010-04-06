/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.DomainSpecificIdentifiers;

/**
 * 
 *
 * @author kirk
 */
public class IdentityIdResolver implements IdResolver {

  @Override
  public DomainSpecificIdentifiers resolve(DomainSpecificIdentifiers ids) {
    return ids;
  }
  

}
