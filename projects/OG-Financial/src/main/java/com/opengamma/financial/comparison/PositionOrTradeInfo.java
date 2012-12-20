/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import java.math.BigDecimal;

import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.security.Security;

/* package */abstract class PositionOrTradeInfo<T extends PositionOrTrade> extends AbstractInfo<T> {

  private final SecurityInfo _security;

  public PositionOrTradeInfo(final ComparisonContext context, final T positionOrTrade) {
    super(positionOrTrade);
    final Security security = positionOrTrade.getSecurity();
    if (security == null) {
      throw new IllegalArgumentException("The security of " + positionOrTrade + " must be resolved");
    }
    _security = new SecurityInfo(context, security);
  }

  public BigDecimal getQuantity() {
    return getUnderlying().getQuantity();
  }

  public SecurityInfo getSecurity() {
    return _security;
  }

  protected boolean equalsImpl(final PositionOrTradeInfo<T> other) {
    return getQuantity().equals(other.getQuantity()) && getSecurity().equals(other.getSecurity());
  }

  protected int hashCodeImpl() {
    int hc = 1;
    hc += (hc << 4) + getQuantity().hashCode();
    hc += (hc << 4) + getSecurity().hashCode();
    return hc;
  }

}
