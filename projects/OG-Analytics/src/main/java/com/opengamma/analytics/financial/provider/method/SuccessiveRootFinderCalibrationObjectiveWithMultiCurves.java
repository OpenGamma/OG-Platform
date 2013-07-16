/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * A function used as objective function for calibration on successive instrument with a root finding process at each of them.
 * But with the additional possibility to set multi curves.
 */
public abstract class SuccessiveRootFinderCalibrationObjectiveWithMultiCurves extends SuccessiveRootFinderCalibrationObjective {

  /**
   * Constructor.
   * @param fxMatrix The exchange rate to convert the present values in a unique currency.
   * @param ccy The unique currency in which all present values are converted.
   */
  public SuccessiveRootFinderCalibrationObjectiveWithMultiCurves(FXMatrix fxMatrix, Currency ccy) {
    super(fxMatrix, ccy);
  }

  /**
   * The data used in the calibration.
   * @param multicurves The multi-curves provider.
   */
  public abstract void setMulticurves(MulticurveProviderInterface multicurves);

}
