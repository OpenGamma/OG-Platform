/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderDiscount;

/**
 * Class describing the data required to price interest rate derivatives with G2++ (curves and parameters).
 * @deprecated Use {@link G2ppProviderDiscount}
 */
@Deprecated
public class G2ppPiecewiseConstantDataBundle extends YieldCurveBundle {

  /**
   * The G2++ model parameters.
   */
  private final G2ppPiecewiseConstantParameters _parameters;

  /**
   * Constructor from G2++ parameters and curve bundle.
   * @param g2ppParameters The G2++ model parameters.
   * @param curves Curve bundle.
   */
  public G2ppPiecewiseConstantDataBundle(final G2ppPiecewiseConstantParameters g2ppParameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(g2ppParameters, "G2++ parameters");
    _parameters = g2ppParameters;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same G2ppPiecewiseConstantParameters is used.
   * @return The bundle.
   */
  public G2ppPiecewiseConstantDataBundle copy() {
    return new G2ppPiecewiseConstantDataBundle(_parameters, this);
  }

  /**
   * Gets the G2++ parameters.
   * @return The parameters.
   */
  public G2ppPiecewiseConstantParameters getG2ppParameter() {
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
    if (!(obj instanceof G2ppPiecewiseConstantDataBundle)) {
      return false;
    }
    final G2ppPiecewiseConstantDataBundle other = (G2ppPiecewiseConstantDataBundle) obj;
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
