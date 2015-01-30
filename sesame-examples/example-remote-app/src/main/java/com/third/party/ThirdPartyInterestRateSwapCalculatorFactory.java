/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.third.party;

import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.irs.InterestRateSwapCalculator;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Example implementation of a third party IRS calculator
 */
public class ThirdPartyInterestRateSwapCalculatorFactory implements InterestRateSwapCalculatorFactory {

  private final DiscountingMulticurveCombinerFn _discountingMulticurveCombinerFn;

  public ThirdPartyInterestRateSwapCalculatorFactory(DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn) {
    _discountingMulticurveCombinerFn = ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
  }


  @Override
  public Result<InterestRateSwapCalculator> createCalculator(Environment env, InterestRateSwapSecurity security) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Result<InterestRateSwapCalculator> createCalculator(Environment environment, InterestRateSwapTrade trade) {
      Result<MulticurveBundle> bundleResult=
        _discountingMulticurveCombinerFn.getMulticurveBundle(environment,trade);

    if (bundleResult.isSuccess()) {
      InterestRateSwapCalculator calculator = new ThirdPartyInterestRateSwapCalculator(trade.getSecurity(),
                                                                                       bundleResult.getValue().getMulticurveProvider(),
                                                                                       environment.getValuationTime());
      return Result.success(calculator);
    } else {
      return Result.failure(bundleResult);
    }
  }
}
