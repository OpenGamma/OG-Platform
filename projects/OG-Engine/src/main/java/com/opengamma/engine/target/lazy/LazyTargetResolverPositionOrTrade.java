/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.math.BigDecimal;

import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;

/* package */abstract class LazyTargetResolverPositionOrTrade extends LazyTargetResolverObject implements PositionOrTrade {

  public LazyTargetResolverPositionOrTrade(final ComputationTargetResolver.AtVersionCorrection resolver, final ComputationTargetSpecification specification) {
    super(resolver, specification);
  }

  protected abstract PositionOrTrade getResolved();

  @Override
  public BigDecimal getQuantity() {
    return getResolved().getQuantity();
  }

  @Override
  public SecurityLink getSecurityLink() {
    return getResolved().getSecurityLink();
  }

  @Override
  public Security getSecurity() {
    return getResolved().getSecurity();
  }

}
