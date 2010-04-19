/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.IdentifierBundle;

/**
 * 
 *
 * @author kirk
 */
public class IdentityIdResolver implements IdResolver {

  @Override
  public IdentifierBundle resolve(IdentifierBundle ids) {
    return ids;
  }
  

}
