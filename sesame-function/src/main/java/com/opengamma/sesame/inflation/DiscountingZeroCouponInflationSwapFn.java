/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.inflation;

import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.trade.ZeroCouponInflationSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Discounting function for calculating risk measures for zero coupon inflation swaps.
 */
public class DiscountingZeroCouponInflationSwapFn implements ZeroCouponInflationSwapFn {

  private final InflationSwapConverterFn _converter;
  private static final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, MultipleCurrencyAmount> CALCULATOR =
      PresentValueDiscountingInflationCalculator.getInstance();

  public DiscountingZeroCouponInflationSwapFn(InflationSwapConverterFn converter) {
    _converter = ArgumentChecker.notNull(converter, "converter");
  }


  @Override
  public Result<MultipleCurrencyAmount> calculatePV(Environment env, ZeroCouponInflationSwapTrade trade) {
    Result<Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative>> defDerivPair = _converter.convert(env, trade.getSecurity());

    //TODO real data
    InflationProviderDiscount data = new InflationProviderDiscount();

    if (defDerivPair.isSuccess()) {

      InstrumentDerivative inflationDerivative = defDerivPair.getValue().getSecond();
      MultipleCurrencyAmount mca = inflationDerivative.accept(CALCULATOR, data);

    }

    return Result.success(MultipleCurrencyAmount.of(Currency.USD, 42));
  }
}
