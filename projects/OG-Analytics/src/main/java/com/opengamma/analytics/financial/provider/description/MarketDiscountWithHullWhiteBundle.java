/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description;

import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.util.money.Currency;

/**
 * Class describing a market bundle based on discount factor for forward rate and with Hull-White parameters for one discount curve (one currency).
 */
public class MarketDiscountWithHullWhiteBundle extends MulticurveProviderDiscount {

  /**
   * The Hull-White one factor model parameters.
   */
  private final HullWhiteOneFactorPiecewiseConstantParameters _parameters;
  /**
   * The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   */
  private final Currency _ccyHW;

  /**
   * Constructor from the Hull-White parameters and an existing market. The maps describing the curves of the market are used (no new maps).
   * @param parameters The Hull-White one factor model parameters.
   * @param ccyHW The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   * @param market The market with curves.
   */
  public MarketDiscountWithHullWhiteBundle(HullWhiteOneFactorPiecewiseConstantParameters parameters, Currency ccyHW, final MulticurveProviderDiscount market) {
    super(market);
    _parameters = parameters;
    _ccyHW = ccyHW;
  }

  @Override
  public MarketDiscountWithHullWhiteBundle copy() {
    MulticurveProviderDiscount curves = super.copy();
    return new MarketDiscountWithHullWhiteBundle(_parameters, _ccyHW, curves);
  }

  /**
   * Returns the Hull-White one factor model parameters.
   * @return The parameters.
   */
  public HullWhiteOneFactorPiecewiseConstantParameters getHullWhiteParameters() {
    return _parameters;
  }

  /**
   * The currency for which the Hull-White parameters are valid (Hull-White on the discounting curve).
   * @return The currency.
   */
  public Currency getHullWhiteCurrency() {
    return _ccyHW;
  }

}
