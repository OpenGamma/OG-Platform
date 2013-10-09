/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

/**
 * Curve sensitivity calculator returning InterestRateSensitivity object.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueCurveSensitivityIRSCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<YieldCurveBundle, InterestRateCurveSensitivity> {
  /**
   * The method unique instance.
   */
  private static final PresentValueCurveSensitivityIRSCalculator INSTANCE = new PresentValueCurveSensitivityIRSCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PresentValueCurveSensitivityIRSCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityIRSCalculator() {
  }

  private static final PresentValueCurveSensitivityCalculator PVCSC = PresentValueCurveSensitivityCalculator.getInstance();

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return new InterestRateCurveSensitivity(instrument.accept(PVCSC, curves));
  }

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curve data");
  }

}
