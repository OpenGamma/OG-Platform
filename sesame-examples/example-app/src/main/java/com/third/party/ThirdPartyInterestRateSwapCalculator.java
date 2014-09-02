package com.third.party;

import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.SwapLegCashFlows;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.sesame.irs.InterestRateSwapCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import org.threeten.bp.ZonedDateTime;

public class ThirdPartyInterestRateSwapCalculator implements InterestRateSwapCalculator {

  public ThirdPartyInterestRateSwapCalculator(InterestRateSwapSecurity security,
                                              MulticurveProviderInterface bundle,
                                              ZonedDateTime valuationTime) {
  }

  /* Simple sample implementation of custom PV */
  @Override
  public Result<MultipleCurrencyAmount> calculatePV() {
    return Result.success(MultipleCurrencyAmount.of(Currency.USD, 42));
  }

  /* Not implemented */
  @Override
  public Result<MultipleCurrencyAmount> calculatePv(MulticurveProviderInterface multicurveProviderInterface) {
    return null;
  }

  /* Not implemented */
  @Override
  public Result<Double> calculateRate() {
    return null;
  }

  /* Not implemented */
  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01() {
    return null;
  }

  /* Not implemented */
  @Override
  public Result<SwapLegCashFlows> calculatePayLegCashFlows() {
    return null;
  }

  /* Not implemented */
  @Override
  public Result<SwapLegCashFlows> calculateReceiveLegCashFlows() {
    return null;
  }

  /* Not implemented */
  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01() {
    return null;
  }

}
