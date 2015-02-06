/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bondfutureoption;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.financial.analytics.conversion.BondFutureOptionTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.trade.BondFutureOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Black calculator for bond future options.
 */
public class BondFutureOptionBlackCalculatorFactory implements BondFutureOptionCalculatorFactory {

  private static final Result<HistoricalTimeSeriesBundle> EMPTY_BUNDLE = Result.success(new HistoricalTimeSeriesBundle());

  private final BondFutureOptionTradeConverter _converter;
  
  private final BlackBondFuturesProviderFn _blackBondFuturesProviderFn;
  
  private final FixingsFn _fixingsFn;

  public BondFutureOptionBlackCalculatorFactory(BondFutureOptionTradeConverter converter,
                                                BlackBondFuturesProviderFn blackBondFuturesProviderFn,
                                                FixingsFn fixingsFn) {
    _converter = ArgumentChecker.notNull(converter, "converter");
    _blackBondFuturesProviderFn = ArgumentChecker.notNull(blackBondFuturesProviderFn, "blackBondFuturesProviderFn");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "htsFn");
  }

  @Override
  public Result<BondFutureOptionCalculator> createCalculator(Environment env, BondFutureOptionTrade trade) {
    
    Result<BlackBondFuturesProviderInterface> blackResult = _blackBondFuturesProviderFn.getBlackBondFuturesProvider(env, trade);
    
    Result<HistoricalTimeSeriesBundle> fixingsResult = getTimeSeries(env, trade);
    
    if (Result.allSuccessful(blackResult, fixingsResult)) {
    
      BlackBondFuturesProviderInterface black = blackResult.getValue();
      
      HistoricalTimeSeriesBundle fixings = fixingsResult.getValue();
      
      BondFutureOptionCalculator calculator = new BondFutureOptionBlackCalculator(trade, _converter, black, env.getValuationTime(), fixings);
      
      return Result.success(calculator);
      
    } else {
      return Result.failure(blackResult, fixingsResult);
    }
  }

  /**
   * Is a time series of margin prices required. Not required if valued on trade date
   *
   * @param valuationDate the valuation date
   * @param trade the trade date
   * @return true if required, else false
   */
  private static boolean requiresTimeSeries(LocalDate valuationDate, BondFutureOptionTrade trade) {
    return !valuationDate.equals(trade.getTrade().getTradeDate());
  }

  private Result<HistoricalTimeSeriesBundle> getTimeSeries(Environment env, BondFutureOptionTrade trade) {
    if (requiresTimeSeries(env.getValuationDate(), trade)) {
      return _fixingsFn.getFixingsForSecurity(env, trade.getSecurity());
    } else {
      return EMPTY_BUNDLE;
    }
  }

}
