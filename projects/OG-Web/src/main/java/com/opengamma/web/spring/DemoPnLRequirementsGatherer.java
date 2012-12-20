/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import com.opengamma.financial.analytics.model.pnl.DefaultPnLRequirementsGatherer;

/**
 *
 */
public class DemoPnLRequirementsGatherer extends DefaultPnLRequirementsGatherer {

  public DemoPnLRequirementsGatherer() {
    addCurveCalculationConfig("USD", "DefaultTwoCurveUSDConfig");
    addFXCurveCalculationConfig("USD", "DefaultTwoCurveUSDConfig");
    addIRFuturesCurveCalculationConfig("USD", "DefaultTwoCurveUSDConfig");
    addFXDiscountingCurveName("USD", "Forward3M");
    addCurveCalculationConfig("EUR", "DefaultTwoCurveEURConfig");
    addFXCurveCalculationConfig("EUR", "DefaultTwoCurveEURConfig");
    addIRFuturesCurveCalculationConfig("EUR", "DefaultTwoCurveEURConfig");
    addFXDiscountingCurveName("EUR", "Forward6M");
    addCurveCalculationConfig("CAD", "DefaultTwoCurveCADConfig");
    addFXCurveCalculationConfig("CAD", "DefaultTwoCurveCADConfig");
    addIRFuturesCurveCalculationConfig("CAD", "DefaultTwoCurveCADConfig");
    addFXDiscountingCurveName("CAD", "Forward3M");
    addCurveCalculationConfig("AUD", "DefaultThreeCurveAUDConfig");
    addFXCurveCalculationConfig("AUD", "DefaultThreeCurveAUDConfig");
    addIRFuturesCurveCalculationConfig("AUD", "DefaultThreeCurveAUDConfig");
    addFXDiscountingCurveName("AUD", "ForwardBasis3M");
    addCurveCalculationConfig("CHF", "DefaultTwoCurveCHFConfig");
    addFXCurveCalculationConfig("CHF", "DefaultTwoCurveCHFConfig");
    addIRFuturesCurveCalculationConfig("CHF", "DefaultTwoCurveCHFConfig");
    addFXDiscountingCurveName("CHF", "Forward6M");
    addCurveCalculationConfig("GBP", "DefaultTwoCurveGBPConfig");
    addFXCurveCalculationConfig("GBP", "DefaultTwoCurveGBPConfig");
    addIRFuturesCurveCalculationConfig("GBP", "DefaultTwoCurveGBPConfig");
    addFXDiscountingCurveName("USD", "Forward3M");
  }

}
