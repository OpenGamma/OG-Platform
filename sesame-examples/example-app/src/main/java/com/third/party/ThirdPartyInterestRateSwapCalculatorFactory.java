package com.third.party;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.irs.InterestRateSwapCalculator;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

public class ThirdPartyInterestRateSwapCalculatorFactory implements InterestRateSwapCalculatorFactory {

  private final DiscountingMulticurveCombinerFn _discountingMulticurveCombinerFn;

  public ThirdPartyInterestRateSwapCalculatorFactory(DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn) {
    _discountingMulticurveCombinerFn = ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
  }

  @Override
  public Result<InterestRateSwapCalculator> createCalculator(Environment environment, InterestRateSwapSecurity security) {
    Result<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> bundleResult =
            _discountingMulticurveCombinerFn.createMergedMulticurveBundle(environment, security, new FXMatrix());

    if (bundleResult.isSuccess()) {
      InterestRateSwapCalculator calculator = new ThirdPartyInterestRateSwapCalculator(security,
                                                                                       bundleResult.getValue().getFirst(),
                                                                                       environment.getValuationTime());
      return Result.success(calculator);
    } else {
      return Result.failure(bundleResult);
    }
  }
}
