/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.derivative;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a Forex swap transaction (with a near and far leg).
 */
public class ForexSwap implements InstrumentDerivative, Serializable {

  /**
   * The near leg.
   */
  private final Forex _nearLeg;
  /**
   * The far leg.
   */
  private final Forex _farLeg;

  /**
   * Constructor from the two Forex legs.
   * @param nearLeg The near leg.
   * @param farLeg The far leg.
   */
  public ForexSwap(final Forex nearLeg, final Forex farLeg) {
    Validate.notNull(nearLeg, "Near leg");
    Validate.notNull(farLeg, "Far leg");
    this._nearLeg = nearLeg;
    this._farLeg = farLeg;
  }

  /**
   * Gets the near leg.
   * @return The near leg.
   */
  public Forex getNearLeg() {
    return _nearLeg;
  }

  /**
   * Gets the far leg.
   * @return The far leg.
   */
  public Forex getFarLeg() {
    return _farLeg;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexSwap(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexSwap(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _farLeg.hashCode();
    result = prime * result + _nearLeg.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForexSwap other = (ForexSwap) obj;
    if (!ObjectUtils.equals(_farLeg, other._farLeg)) {
      return false;
    }
    if (!ObjectUtils.equals(_nearLeg, other._nearLeg)) {
      return false;
    }
    return true;
  }

}
