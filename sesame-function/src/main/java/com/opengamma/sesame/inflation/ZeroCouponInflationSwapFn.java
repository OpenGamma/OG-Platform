/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.inflation;

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



}
