/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * This {@code IdResolver} returns the ID as-is.  
 *
 */
public class IdentityIdResolver extends AbstractResolver<IdentifierBundle, Identifier> implements IdResolver {

  @Override
  public Identifier resolve(IdentifierBundle ids) {
    if (ids.getIdentifiers().size() != 1) {
      throw new IllegalArgumentException("This resolver only supports singleton bundles");
    }
    return ids.getIdentifiers().iterator().next();
  }
  

}
