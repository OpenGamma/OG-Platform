/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveAddFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.SeasonalCurve;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.math.curve.AddCurveSpreadFunction;
import com.opengamma.analytics.math.curve.SpreadDoublesCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The curve is the sum (or the multiplication) of two curves 
 * (operation on the continuously-compounded zero-coupon rates): an existing curve referenced by its name and a new curve. 
 * The generated curve is a PriceIndexCurve.
 */
public class GeneratorPriceIndexCurveAddSeasonality extends GeneratorPriceIndexCurve {

  /**
   * The generator for the new curve.
   */
  private final GeneratorPriceIndexCurve _generator;
  /**
   * The name of the existing curve.
   */
  private final SeasonalCurve _seasonalCurve;

  /**
   * The constructor.
   * @param generator The generator for the new curve.
   * @param seasonalCurve The seasonal curve.
   */
  public GeneratorPriceIndexCurveAddSeasonality(final GeneratorPriceIndexCurve generator, final SeasonalCurve seasonalCurve) {
    ArgumentChecker.notNull(generator, "Generator");
    ArgumentChecker.notNull(seasonalCurve, "Seasonal curve");
    _generator = generator;
    _seasonalCurve = seasonalCurve;
  }

  @Override
  public int getNumberOfParameter() {
    return _generator.getNumberOfParameter();
  }

  @Override
  public PriceIndexCurveSimple generateCurve(final String name, final double[] parameters) {
    final PriceIndexCurveSimple newCurve = _generator.generateCurve(name, parameters);
    return new PriceIndexCurveSimple(SpreadDoublesCurve.from(new AddCurveSpreadFunction(), name, newCurve.getCurve(), _seasonalCurve));
  }

  @Override
  public PriceIndexCurveSimple generateCurve(final String name, final InflationProviderInterface inflation, final double[] parameters) {
    final PriceIndexCurveSimple newCurve = _generator.generateCurve(name, inflation, parameters);
    return new PriceIndexCurveAddFixedCurve(name, newCurve, _seasonalCurve);
  }

  @Override
  public GeneratorPriceIndexCurve finalGenerator(final Object data) {
    return new GeneratorPriceIndexCurveAddSeasonality(_generator.finalGenerator(data), _seasonalCurve);
  }

  @Override
  public double[] initialGuess(final double[] rates) {
    return _generator.initialGuess(rates);
  }

}
