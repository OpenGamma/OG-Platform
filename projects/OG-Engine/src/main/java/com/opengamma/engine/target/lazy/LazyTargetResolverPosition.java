/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;

/**
 * A position that may not be fully resolved at construction but will appear fully resolved when used.
 */
public class LazyTargetResolverPosition extends LazyTargetResolverPositionOrTrade implements Position {

  public LazyTargetResolverPosition(final ComputationTargetResolver.AtVersionCorrection resolver, final ComputationTargetSpecification specification) {
    super(resolver, specification);
  }

  protected Position getResolved() {
    return getResolvedTarget().getPosition();
  }

  @Override
  public Collection<Trade> getTrades() {
    return getResolved().getTrades();
  }

  @Override
  public Map<String, String> getAttributes() {
    return getResolved().getAttributes();
  }

}
