/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Calculates the change in value of a swaption when the curves and (Black) surface have been
 * shifted forward in time without slide.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class ConstantSpreadSwaptionBlackRolldown implements RolldownFunction<YieldCurveWithBlackSwaptionBundle> {
  private static final ConstantSpreadYieldCurveBundleRolldownFunction CURVES_ROLLDOWN = ConstantSpreadYieldCurveBundleRolldownFunction.getInstance();
  private static final ConstantSpreadSurfaceRolldownFunction SURFACE_ROLLDOWN = ConstantSpreadSurfaceRolldownFunction.getInstance();
  private static final ConstantSpreadSwaptionBlackRolldown INSTANCE = new ConstantSpreadSwaptionBlackRolldown();

  public static ConstantSpreadSwaptionBlackRolldown getInstance() {
    return INSTANCE;
  }

  private ConstantSpreadSwaptionBlackRolldown() {
  }

  @Override
  public YieldCurveWithBlackSwaptionBundle rollDown(final YieldCurveWithBlackSwaptionBundle data, final double time) {
    final YieldCurveBundle shiftedCurves = CURVES_ROLLDOWN.rollDown(data, time);
    final Surface<Double, Double, Double> surface = data.getBlackParameters().getVolatilitySurface();
    final Surface<Double, Double, Double> shiftedVolatilitySurface = SURFACE_ROLLDOWN.rollDown(surface, time);
    final BlackFlatSwaptionParameters shiftedParameters = new BlackFlatSwaptionParameters(shiftedVolatilitySurface, data.getBlackParameters().getGeneratorSwap());
    return new YieldCurveWithBlackSwaptionBundle(shiftedParameters, shiftedCurves);
  }
}
