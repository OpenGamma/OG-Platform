/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bondfuture;

import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeConverter;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.IssuerProviderBundle;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.trade.BondFutureTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Implementation of the BondFutureCalculatorFactory that uses the discounting calculator to return values.
 */
public class BondFutureDiscountingCalculatorFactory implements BondFutureCalculatorFactory {

  private final BondAndBondFutureTradeConverter _converter;
  private final IssuerProviderFn _issuerProviderFn;

  public BondFutureDiscountingCalculatorFactory(BondAndBondFutureTradeConverter converter,
                                                IssuerProviderFn issuerProviderFn) {
    _converter = ArgumentChecker.notNull(converter, "converter");
    _issuerProviderFn = ArgumentChecker.notNull(issuerProviderFn, "issuerProviderFn");
  }
  
  @Override
  public Result<BondFutureDiscountingCalculator> createCalculator(Environment env, BondFutureTrade bondFutureTrade) {

    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, bondFutureTrade.getTrade());

    if (bundleResult.isSuccess()) {
      ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
      BondFutureDiscountingCalculator calculator = new BondFutureDiscountingCalculator(bondFutureTrade,
                                                                                       curves,
                                                                                       _converter,
                                                                                       env.getValuationTime());
      return Result.success(calculator);
    } else {
      return Result.failure(bundleResult);
    }
  }


}
