/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.third.party;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCrossSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.analytics.model.fixedincome.SwapLegCashFlows;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.sesame.irs.InterestRateSwapCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Example implementation of a third party IRS calculator
 */
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
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<Double> calculateRate() {
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<Double> calculateParSpread() {
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01() {
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<SwapLegCashFlows> calculatePayLegCashFlows() {
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<SwapLegCashFlows> calculateReceiveLegCashFlows() {
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<MultipleCurrencyAmount> calculatePayLegPv() {
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<MultipleCurrencyAmount> calculateReceiveLegPv() {
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01() {
    throw new UnsupportedOperationException();
  }

  /* Not implemented */
  @Override
  public Result<BucketedCrossSensitivities> calculateBucketedGamma() {
    throw new UnsupportedOperationException();
  }

}
