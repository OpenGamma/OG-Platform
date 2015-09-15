/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.inflation;

import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.trade.ZeroCouponInflationSwapTrade;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;

/**
 * Calculate analytics values for a zero coupon inflation swap.
 */
public interface ZeroCouponInflationSwapFn {
  
  /* Security based model integration */

  /**
   * Calculate the present value for a zero coupon inflation trade.
   *
   * @param env the environment used for calculation
   * @param trade the ZeroCouponInflationSwapTrade to calculate the PV for
   * @return result containing the present value if successful, a Failure otherwise
   */
  @Output(OutputNames.PRESENT_VALUE)
  Result<MultipleCurrencyAmount> calculatePV(Environment env, ZeroCouponInflationSwapTrade trade);

  /**
   * Calculate the bucketed PV01 for a zero coupon inflation trade.
   *
   * @param env the environment used for calculation
   * @param trade the ZeroCouponInflationSwapTrade to calculate the PV for
   * @return result containing the bucketed PV01 if successful, a Failure otherwise
   */
  @Output(OutputNames.BUCKETED_PV01)
  Result<BucketedCurveSensitivities> calculateBucketedPV01(Environment env, ZeroCouponInflationSwapTrade trade);

  /**
   * Calculate the par rate for a zero coupon inflation trade.
   *
   * @param env the environment used for calculation
   * @param trade the ZeroCouponInflationSwapTrade to calculate the par rate for
   * @return result containing the par rate if successful, a Failure otherwise
   */
  @Output(OutputNames.PAR_RATE)
  Result<Double> calculateParRate(Environment env, ZeroCouponInflationSwapTrade trade);

}
