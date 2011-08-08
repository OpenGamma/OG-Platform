/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * This {@code IdResolver} returns the ID as-is.  
 *
 */
public class IdentityIdResolver extends AbstractResolver<ExternalIdBundle, ExternalId> implements IdResolver {

  @Override
  public ExternalId resolve(ExternalIdBundle ids) {
    if (ids.getExternalIds().size() != 1) {
      throw new IllegalArgumentException("This resolver only supports singleton bundles");
    }
    return ids.getExternalIds().iterator().next();
  }
  

}
