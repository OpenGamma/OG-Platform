/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Provider of normal (Bachelier) smile for swaptions. 
 * The volatility is time to expiration/tenor dependent. 
 */
public class NormalSwaptionExpiryTenorProvider implements NormalSwaptionProviderInterface, SwaptionSurfaceProvider {

  /** The multicurve provider. */
  private final MulticurveProviderInterface _multicurve;
  /** The normal (Bachelier) volatility surface. Not null. 
   *  The dimensions are expiration and tenor. */
  private final Surface<Double, Double, Double> _parameters;
  /** The underlying swaps generators. */
  private final GeneratorSwapFixedIbor _generator;

  /**
   * Constructor.
   * @param multicurve The multi-curve provider. Not null.
   * @param parameters The normal volatility parameters. Not null.
   * @param generator The underlying swaps generators. Not null.
   */
  public NormalSwaptionExpiryTenorProvider(final MulticurveProviderInterface multicurve, 
      final Surface<Double, Double, Double> parameters, final GeneratorSwapFixedIbor generator) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(generator, "swap generator");
    _multicurve = multicurve;
    _parameters = parameters;
    _generator = generator;
  }

  @Override
  public NormalSwaptionExpiryTenorProvider copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurve.copy();
    return new NormalSwaptionExpiryTenorProvider(multicurveProvider, _parameters, _generator);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurve;
  }

  @Override
  public double getVolatility(double expiry, double tenor, double strikeRate, double forwardRate) {
    return _parameters.getZValue(expiry, tenor);
  }

  @Override
  public GeneratorSwapFixedIbor getGeneratorSwap() {
    return _generator;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurve.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurve.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurve.getAllCurveNames();
  }
  
  /**
   * Returns the The normal (Bachelier) volatility surface. The dimensions are expiration and tenor.
   * @return The surface
   */
  @Override
  public Surface<Double, Double, Double> getParameterSurface() {
    return _parameters;
  }

}
