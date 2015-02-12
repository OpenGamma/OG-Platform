/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irfutureoption;

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
public class IRFutureOptionFnUtils {

  private IRFutureOptionFnUtils() { /* private constructor */ }

  public static FuturesTransaction<InterestRateFutureOptionSecurity> createDerivative(IRFutureOptionTrade tradeWrapper,
                                                      InterestRateFutureOptionTradeConverter converter,
                                                      ZonedDateTime valTime,
                                                      FixedIncomeConverterDataProvider definitionToDerivativeConverter,
                                                      HistoricalTimeSeriesBundle fixings) {
    InstrumentDefinition<?> definition = converter.convert(tradeWrapper.getTrade());
    InstrumentDerivative instrumentDerivative =
        definitionToDerivativeConverter.convert(tradeWrapper.getSecurity(), definition, valTime, fixings);
    return (FuturesTransaction<InterestRateFutureOptionSecurity>) instrumentDerivative;
  }

}
