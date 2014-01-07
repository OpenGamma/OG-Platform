/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;

/**
 * Class describing the data required to price interest rate derivatives with Hull-White one factor model (curves and model parameters).
 * @deprecated Use {@link HullWhiteOneFactorProviderDiscount}
 */
@Deprecated
public class HullWhiteOneFactorPiecewiseConstantDataBundle extends YieldCurveBundle {

  /**
   * The Hull-White one factor model parameters.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _parameters;

  /**
   * Constructor from Hull-White parameters and curve bundle.
   * @param hullWhiteParameters The Hull-White model parameters.
   * @param curves Curve bundle.
   */
  public HullWhiteOneFactorPiecewiseConstantDataBundle(final HullWhiteOneFactorPiecewiseConstantParameters hullWhiteParameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(hullWhiteParameters, "Hull-White parameters");
    _parameters = hullWhiteParameters;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same HullWhiteOneFactorPiecewiseConstantParameters is used.
   * @return The bundle.
   */
  public HullWhiteOneFactorPiecewiseConstantDataBundle copy() {
    return new HullWhiteOneFactorPiecewiseConstantDataBundle(_parameters, this);
  }

  /**
   * Gets the Hull-White one factor parameters.
   * @return The parameters.
   */
  public HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameter() {
    return _parameters;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof HullWhiteOneFactorPiecewiseConstantDataBundle)) {
      return false;
    }
    final HullWhiteOneFactorPiecewiseConstantDataBundle other = (HullWhiteOneFactorPiecewiseConstantDataBundle) obj;
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
