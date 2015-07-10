/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.parameters;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.financial.provider.description.interestrate.SwaptionSurfaceProvider;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the Black volatility surface used in swaption modeling.
 */
public class BlackFlatSwaptionParameters implements VolatilityModel<double[]>, SwaptionSurfaceProvider {

  /**
   * The volatility surface. The first dimension is the expiration and the second the underlying swap tenor.
   */
  private final Surface<Double, Double, Double> _volatility;
  /**
   * The standard swap generator (in particular fixed leg convention and floating leg tenor) for which the volatility surface is valid.
   */
  private final GeneratorInstrument<GeneratorAttributeIR> _generatorSwap;

  /**
   * Constructor from the parameter surfaces.
   * @param volatility The Black volatility surface. The first dimension is the expiration and the second the underlying swap tenor.
   * @param generatorSwap The standard swap generator for which the volatility surface is valid.
   */
  public BlackFlatSwaptionParameters(final Surface<Double, Double, Double> volatility, final GeneratorInstrument<GeneratorAttributeIR> generatorSwap) {
    ArgumentChecker.notNull(volatility, "volatility");
    ArgumentChecker.notNull(generatorSwap, "generatorSwap");
    _volatility = volatility;
    _generatorSwap = generatorSwap;
  }

  /**
   * Return the volatility for a pair of time to expiration and instrument tenor.
   * @param expiryMaturity The expiration/tenor pair.
   * @return The volatility.
   */
  public double getVolatility(final DoublesPair expiryMaturity) {
    return _volatility.getZValue(expiryMaturity);
  }

  /**
   * Return the volatility for a pair of time to expiration and instrument tenor.
   * @param expiry The time to expiration
   * @param maturity The expiration/tenor pair.
   * @return The volatility.
   */
  public double getVolatility(final double expiry, final double maturity) {
    return _volatility.getZValue(expiry, maturity);
  }

  @Override
  /**
   * Return the volatility for a expiration/instrument tenor array.
   * @param data An array of two doubles with [0] the expiration, [1] instrument tenor.
   * @return The volatility.
   */
  public Double getVolatility(final double[] data) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.length == 2, "data should have two components (expiration, instrument tenor)");
    return getVolatility(data[0], data[1]);
  }

  /**
   * Gets the standard swap generator for which the volatility surface is valid.
   * @return The swap generator.
   */
  @Override
  public GeneratorInstrument<GeneratorAttributeIR> getGeneratorSwap() {
    return _generatorSwap;
  }

  /**
   * Gets the volatility surface.
   * @return The volatility surface.
   */
  @Override
  public Surface<Double, Double, Double> getParameterSurface() {
    return _volatility;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _generatorSwap.hashCode();
    result = prime * result + _volatility.hashCode();
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
    final BlackFlatSwaptionParameters other = (BlackFlatSwaptionParameters) obj;
    if (!ObjectUtils.equals(_generatorSwap, other._generatorSwap)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}
