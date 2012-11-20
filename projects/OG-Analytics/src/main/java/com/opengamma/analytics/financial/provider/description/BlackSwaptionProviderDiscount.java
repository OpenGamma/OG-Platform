/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.option.definition.BlackSwaptionParameters;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing curve and volatility data sufficient to price swaptions using the Black method.
 * The forward rates are computed using discount factors.
 */
public class BlackSwaptionProviderDiscount implements BlackSwaptionProviderInterface {
  private final MulticurveProviderDiscount _multiCurveProvider;
  private final BlackSwaptionParameters _blackParameters;

  /**
   * @param multiCurveProvider The multi-curve provider, not null
   * @param blackParameters The Black parameters, not null
   */
  public BlackSwaptionProviderDiscount(final MulticurveProviderDiscount multiCurveProvider, final BlackSwaptionParameters blackParameters) {
    ArgumentChecker.notNull(multiCurveProvider, "multi-curve provider");
    ArgumentChecker.notNull(blackParameters, "Black parameters");
    _multiCurveProvider = multiCurveProvider.copy();
    _blackParameters = blackParameters; //TODO copy
  }

  @Override
  public BlackSwaptionProviderInterface copy() {
    final MulticurveProviderDiscount curves = _multiCurveProvider.copy();
    final BlackSwaptionParameters black = _blackParameters; //TODO copy these parameters
    return new BlackSwaptionProviderDiscount(curves, black);
  }

  @Override
  public BlackSwaptionParameters getBlackParameters() {
    return _blackParameters;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multiCurveProvider;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _blackParameters.hashCode();
    result = prime * result + _multiCurveProvider.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackSwaptionProviderDiscount)) {
      return false;
    }
    final BlackSwaptionProviderDiscount other = (BlackSwaptionProviderDiscount) obj;
    if (!ObjectUtils.equals(_blackParameters, other._blackParameters)) {
      return false;
    }
    if (!ObjectUtils.equals(_multiCurveProvider, other._multiCurveProvider)) {
      return false;
    }
    return true;
  }


}
