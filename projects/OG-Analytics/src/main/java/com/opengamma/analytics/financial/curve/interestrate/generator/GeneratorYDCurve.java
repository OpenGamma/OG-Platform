/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.generator;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Interface for describing and generating curves in curve construction process.
 */
@SuppressWarnings("deprecation")
public abstract class GeneratorYDCurve extends GeneratorCurve {

  /**
   * Generate a curve using the parameters of a vector.
   * @param name The curve name.
   * @param parameters The parameters.
   * @return The curve.
   */
  abstract YieldAndDiscountCurve generateCurve(final String name, final double[] parameters);

  /**
   * Generate a curve using the parameters of a vector and an existing bundle. The existing bundle will be required if the generated curve depends on previous curves.
   * @param name The curve name.
   * @param bundle The bundle of existing curves.
   * @param parameters The parameters.
   * @return The curve.
   * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
   */
  @Deprecated
  public abstract YieldAndDiscountCurve generateCurve(final String name, final YieldCurveBundle bundle, final double[] parameters);

  /**
   * Generate a curve using the parameters of a vector and an existing bundle. The existing bundle will be required if the generated curve depends on previous curves.
   * @param name The curve name.
   * @param multicurves The multi-curves provider.
   * @param parameters The parameters.
   * @return The curve.
   */
  public abstract YieldAndDiscountCurve generateCurve(final String name, final MulticurveProviderInterface multicurves, final double[] parameters);

  /**
   * Generate a curve using the parameters of a vector and an existing bundle. The existing bundle will be required if the generated curve depends on previous curves.
   * @param name The curve name.
   * @param hwMulticurves The Hull-White one-factor with multi-curves provider.
   * @param parameters The parameters.
   * @return The curve.
   */
  public YieldAndDiscountCurve generateCurve(final String name, final HullWhiteOneFactorProviderDiscount hwMulticurves, final double[] parameters) {
    return generateCurve(name, hwMulticurves.getMulticurveProvider(), parameters);
  }

  /**
   * Some generators require a two stage process. The generator with the general description (like interpolated) and
   * a specific one with all the details (like the node times for the interpolated).
   * The method create the specific generator from the generic one.
   * @param data The additional data.
   * @return The final generator.
   */
  @Override
  public GeneratorYDCurve finalGenerator(final Object data) {
    return this;
  }

}
