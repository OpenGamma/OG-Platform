/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * A function used as objective function for calibration on successive instrument with a least square process for each of them.
 * But with the additional possibility to set inflation curves.
 * The least square is done on the prices.
 */
public abstract class SuccessiveLeastSquareCalibrationObjectiveWithInflation extends SuccessiveLeastSquareCalibrationObjective {

  /**
   * Constructor.
   * @param fxMatrix The exchange rate to convert the present values in a unique currency.
   * @param ccy The unique currency in which all present values are converted.
   */
  public SuccessiveLeastSquareCalibrationObjectiveWithInflation(FXMatrix fxMatrix, Currency ccy) {
    super(fxMatrix, ccy);
  }

  /**
   * The data used in the calibration.
   * @param inflation The infation provider.
   */
  public abstract void setInflation(InflationProviderInterface inflation);

}
