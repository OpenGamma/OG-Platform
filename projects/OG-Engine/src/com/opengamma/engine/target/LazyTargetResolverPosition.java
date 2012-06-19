/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;

/* package */class LazyTargetResolverPosition extends LazyTargetResolverPositionOrTrade implements Position {

  public LazyTargetResolverPosition(final ComputationTargetResolver resolver, final ComputationTargetSpecification specification) {
    super(resolver, specification);
  }

  protected Position getResolved() {
    return getResolvedTarget().getPosition();
  }

  @Override
  public UniqueId getParentNodeId() {
    return getResolved().getParentNodeId();
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
