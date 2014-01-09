/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

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
   * @param multicurveProvider The multicurve provider, not null
   * @param parameters The SABR parameters, not null
   * @param generator The underlying swaps generators, not null
   */
  public SABRSwaptionProvider(final MulticurveProviderInterface multicurveProvider, final SABRInterestRateParameters parameters, final GeneratorSwapFixedIbor generator) {
    ArgumentChecker.notNull(multicurveProvider, "multicurveProvider");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(generator, "generator");
    _multicurveProvider = multicurveProvider;
    _parameters = parameters;
    _generator = generator;
  }

  @Override
  public SABRSwaptionProviderInterface copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
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

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurveProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _generator.hashCode();
    result = prime * result + _multicurveProvider.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SABRSwaptionProvider)) {
      return false;
    }
    final SABRSwaptionProvider other = (SABRSwaptionProvider) obj;
    if (!ObjectUtils.equals(_generator, other._generator)) {
      return false;
    }
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
