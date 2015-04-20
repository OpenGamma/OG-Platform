/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irfutureoption;


import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionSecurity;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.sesame.trade.IRFutureOptionTrade;

/**
 * Utility class for interest rate options functions
 */
public final class IRFutureOptionFnUtils {

  private IRFutureOptionFnUtils() { /* private constructor */ }

  /**
   * Is a time series of margin prices required. Not required if valued on trade date
   *
   * @param tradeWrapper the IRFutureOptionTrade trade
   * @param converter converter used to create the definition of the interest rate future option, not null.
   * @param valTime the ZonedDateTime valuation time
   * @param definitionToDerivativeConverter converter used to create the derivative of the future option, not null.
   * @param fixings function used to retrieve the historical prices of the underlying interest rate future.
   *
   * @return FuturesTransaction<InterestRateFutureOptionSecurity> instrument derivative
   */
  public static FuturesTransaction<InterestRateFutureOptionSecurity> createDerivative(
      IRFutureOptionTrade tradeWrapper,
      InterestRateFutureOptionTradeConverter converter,
      ZonedDateTime valTime,
      FixedIncomeConverterDataProvider definitionToDerivativeConverter,
      HistoricalTimeSeriesBundle fixings) {
    InstrumentDefinition<?> definition = converter.convert(tradeWrapper.getTrade());
    InstrumentDerivative instrumentDerivative =
        definitionToDerivativeConverter.convert(tradeWrapper.getSecurity(), definition, valTime, fixings);
    return (FuturesTransaction<InterestRateFutureOptionSecurity>) instrumentDerivative;
  }

  /**
   * Is a time series of margin prices required. Not required if valued on trade date
   *
   * @param valuationDate the valuation date
   * @param trade the trade date
   * @return true if required, else false
   */
  public static boolean requiresTimeSeries(LocalDate valuationDate, IRFutureOptionTrade trade) {
    return !valuationDate.equals(trade.getTrade().getTradeDate());
  }

}
