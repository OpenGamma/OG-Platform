/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;

/**
 * Implementation for swaption SABR parameters provider for one underlying when multi-curves are described by a MulticurveProviderDiscount.
 */
public class SABRSwaptionProvider implements SABRSwaptionProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The SABR parameters.
   */
  private final SABRInterestRateParameters _parameters;
  /**
   * The underlying swaps generators.
   */
  private final GeneratorSwapFixedIbor _generator;

  /**
   * @param multicurveProvider The multicurve provider.
   * @param parameters The SABR parameters.
   * @param generator The underlying swaps generators.
   */
  public SABRSwaptionProvider(MulticurveProviderInterface multicurveProvider, SABRInterestRateParameters parameters, GeneratorSwapFixedIbor generator) {
    _multicurveProvider = multicurveProvider;
    _parameters = parameters;
    _generator = generator;
  }

  @Override
  public SABRSwaptionProviderInterface copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new SABRSwaptionProvider(multicurveProvider, _parameters, _generator);
  }

  @Override
  public SABRInterestRateParameters getSABRParameter() {
    return _parameters;
  }

  @Override
  public GeneratorSwapFixedIbor getSABRGenerator() {
    return _generator;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

}
