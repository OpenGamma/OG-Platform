/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import java.math.BigDecimal;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;

/**
 * A position or trade implementation that defers to a target resolver for the component parts.
 */
/* package */abstract class TargetResolverPositionOrTrade extends TargetResolverObject implements PositionOrTrade {

  private final UniqueId _uniqueId;
  private final BigDecimal _quantity;
  private final ComputationTargetSpecification _securitySpec;
  private transient volatile ComputationTarget _security;

  public TargetResolverPositionOrTrade(final ComputationTargetResolver.AtVersionCorrection targetResolver, final PositionOrTrade copyFrom) {
    super(targetResolver);
    _uniqueId = copyFrom.getUniqueId();
    _quantity = copyFrom.getQuantity();
    _securitySpec = ComputationTargetSpecification.of(copyFrom.getSecurity());
  }

  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  @Override
  public BigDecimal getQuantity() {
    return _quantity;
  }

  @Override
  public SecurityLink getSecurityLink() {
    return SimpleSecurityLink.of(getSecurity());
  }

  @Override
  public Security getSecurity() {
    if (_security == null) {
      synchronized (this) {
        if (_security == null) {
          _security = getTargetResolver().resolve(_securitySpec);
          if (_security == null) {
            throw new OpenGammaRuntimeException("Couldn't resolve " + _securitySpec);
          }
        }
      }
    }
    return _security.getSecurity();
  }

}
