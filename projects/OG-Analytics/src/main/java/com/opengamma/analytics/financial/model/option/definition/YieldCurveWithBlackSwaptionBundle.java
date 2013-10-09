/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderDiscount;

/**
 * Class describing the data required to price swaptions with Black (curves and volatility).
 * @deprecated Use {@link BlackSwaptionFlatProviderDiscount}
 */
@Deprecated
public class YieldCurveWithBlackSwaptionBundle extends YieldCurveBundle {

  /**
   * The Black volatility surface. Not null.
   */
  private final BlackFlatSwaptionParameters _parameters;

  /**
   * Constructor from Black volatility surface and curve bundle.
   * @param parameters The Black volatility surface.
   * @param curves Curve bundle.
   */
  public YieldCurveWithBlackSwaptionBundle(final BlackFlatSwaptionParameters parameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(parameters, "Volatility surface");
    _parameters = parameters;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same BlackSwaptionParameters is used.
   * @return The bundle.
   */
  public YieldCurveWithBlackSwaptionBundle copy() {
    return new YieldCurveWithBlackSwaptionBundle(_parameters, this);
  }

  /**
   * Gets the Black volatility surface.
   * @return The surface.
   */
  public BlackFlatSwaptionParameters getBlackParameters() {
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurveWithBlackSwaptionBundle other = (YieldCurveWithBlackSwaptionBundle) obj;
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
