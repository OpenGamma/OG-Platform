/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;

/**
 * Interface for describing and generating inflation curves in curve construction process.
 */
public abstract class GeneratorPriceIndexCurve extends GeneratorCurve {

  /**
   * Generate a curve using the parameters of a vector.
   * @param name The curve name.
   * @param parameters The parameters.
   * @return The curve.
   */
  abstract PriceIndexCurveSimple generateCurve(final String name, final double[] parameters);

  /**
   * Generate a curve using the parameters of a vector and an existing bundle. The existing bundle will be required if the generated curve depends on previous curves.
   * @param name The curve name.
   * @param inflation The multi-curves provider.
   * @param parameters The parameters.
   * @return The curve.
   */
  public abstract PriceIndexCurveSimple generateCurve(final String name, final InflationProviderInterface inflation, final double[] parameters);

  /**
   * Generate a curve using the parameters of a vector and an existing bundle. The existing bundle will be required if the generated curve depends on previous curves.
   * @param name The curve name.
   * @param inflation The multi-curves provider.
   * @param parameters The parameters.
   * @return The curve.
   */
  public PriceIndexCurveSimple generateCurve(final String name, final InflationIssuerProviderInterface inflation, final double[] parameters) {
    return generateCurve(name, inflation.getInflationProvider(), parameters);
  }

  /**
   * Some generators require a two stage process. The generator with the general description (like interpolated) and 
   * a specific one with all the details (like the node times for the interpolated). 
   * The method create the specific generator from the generic one.
   * @param data The additional data.
   * @return The final generator.
   */
  @Override
  public GeneratorPriceIndexCurve finalGenerator(final Object data) {
    return this;
  }

}
