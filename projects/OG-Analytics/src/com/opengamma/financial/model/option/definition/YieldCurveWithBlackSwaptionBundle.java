/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Class describing the data required to price swaptions with Black (curves and volatility).
 */
public class YieldCurveWithBlackSwaptionBundle extends YieldCurveBundle {

  /**
   * The Black volatility surface. Not null.
   */
  private final BlackSwaptionParameters _parameters;

  /**
   * Constructor from Black volatility surface and curve bundle.
   * @param parameters The Black volatility surface.
   * @param curves Curve bundle.
   */
  public YieldCurveWithBlackSwaptionBundle(final BlackSwaptionParameters parameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(parameters, "Volatility surface");
    _parameters = parameters;
  }

  /**
   * Gets the Black volatility surface.
   * @return The surface.
   */
  public BlackSwaptionParameters getBlackParameters() {
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    YieldCurveWithBlackSwaptionBundle other = (YieldCurveWithBlackSwaptionBundle) obj;
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
