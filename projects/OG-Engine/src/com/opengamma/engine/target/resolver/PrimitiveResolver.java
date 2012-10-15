/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.resolver;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;

/**
 * A {@link Resolver} for {@link ComputationTargetType#PRIMITIVE}.
 */
public class PrimitiveResolver implements Resolver<UniqueId> {

  @Override
  public UniqueId resolve(final UniqueId uniqueId) {
    return uniqueId;
  }

}
