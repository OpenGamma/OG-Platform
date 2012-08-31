/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;


/**
 * Curve sensitivity calculator returning InterestRateSensitivity object.
 */
public class PresentValueCurveSensitivityIRSCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> {
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
    return new InterestRateCurveSensitivity(PVCSC.visit(instrument, curves));
  }

}
