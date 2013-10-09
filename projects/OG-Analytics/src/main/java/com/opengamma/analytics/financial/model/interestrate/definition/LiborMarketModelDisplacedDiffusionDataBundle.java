/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProviderDiscount;

/**
 * Class describing the data required to price interest rate derivatives with LMM displaced diffusion (curves and parameters).
 * @deprecated Use {@link LiborMarketModelDisplacedDiffusionProviderDiscount}
 */
@Deprecated
public class LiborMarketModelDisplacedDiffusionDataBundle extends YieldCurveBundle {

  /**
   * The LMM parameters.
   */
  private final LiborMarketModelDisplacedDiffusionParameters _parameters;

  /**
   * Constructor from LMM parameters and curve bundle.
   * @param lmmParameters The LMM model parameters.
   * @param curves Curve bundle.
   */
  public LiborMarketModelDisplacedDiffusionDataBundle(final LiborMarketModelDisplacedDiffusionParameters lmmParameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(lmmParameters, "LMM parameters");
    _parameters = lmmParameters;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same LiborMarketModelDisplacedDiffusionParameters is used.
   * @return The bundle.
   */
  public LiborMarketModelDisplacedDiffusionDataBundle copy() {
    return new LiborMarketModelDisplacedDiffusionDataBundle(_parameters, this);
  }

  /**
   * Gets the G2++ parameters.
   * @return The parameters.
   */
  public LiborMarketModelDisplacedDiffusionParameters getLmmParameter() {
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
    if (!(obj instanceof LiborMarketModelDisplacedDiffusionDataBundle)) {
      return false;
    }
    final LiborMarketModelDisplacedDiffusionDataBundle other = (LiborMarketModelDisplacedDiffusionDataBundle) obj;
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
