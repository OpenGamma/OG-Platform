/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bondfuture;

import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
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
  
  private final FixingsFn _fixingsFn;
  
  public BondFutureDiscountingCalculatorFactory(BondAndBondFutureTradeConverter converter,
                                                IssuerProviderFn issuerProviderFn,
                                                FixingsFn fixingsFn) {
    _converter = ArgumentChecker.notNull(converter, "converter");
    _issuerProviderFn = ArgumentChecker.notNull(issuerProviderFn, "issuerProviderFn");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "fixingsFn");
  }
  
  @Override
  public Result<BondFutureDiscountingCalculator> createCalculator(Environment env,
                                               BondFutureTrade bondFutureTrade) {
    FinancialSecurity security = bondFutureTrade.getSecurity();

    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, bondFutureTrade.getTrade());
    
    Result<HistoricalTimeSeriesBundle> fixingsResult = _fixingsFn.getFixingsForSecurity(env, security);
    
    if (Result.allSuccessful(bundleResult, fixingsResult)) {
      ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();
      
      HistoricalTimeSeriesBundle fixings = fixingsResult.getValue();
      
      BondFutureDiscountingCalculator calculator =
          new BondFutureDiscountingCalculator(bondFutureTrade, curves, _converter, env.getValuationTime(), fixings);
      
      return Result.success(calculator);
    } else {
      return Result.failure(bundleResult, fixingsResult);
    }
  }
}
