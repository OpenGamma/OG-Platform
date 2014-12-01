/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateSwapSecurityConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Converts an {@link InterestRateSwapSecurity} into an {@link InstrumentDefinition} and {@link InstrumentDerivative}.
 */
public class DefaultInterestRateSwapConverterFn implements InterestRateSwapConverterFn {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultInterestRateSwapConverterFn.class);

  private final InterestRateSwapSecurityConverter _secToDefnConverter;
  private final FixedIncomeConverterDataProvider _defnToDerivConverter;
  private final FixingsFn _fixingsFn;

  /**
   * @param secToDefnConverter converts an {@link InterestRateSwapSecurity} to a {@link SwapDefinition}
   * @param defnToDerivConverter converts a {@link SwapDefinition} to a {@link InstrumentDerivative}
   * @param fixingsFn provides time series of fixings for the security
   */
  public DefaultInterestRateSwapConverterFn(InterestRateSwapSecurityConverter secToDefnConverter,
                                            FixedIncomeConverterDataProvider defnToDerivConverter,
                                            FixingsFn fixingsFn) {
    _secToDefnConverter = ArgumentChecker.notNull(secToDefnConverter, "secToDefnConverter");
    _defnToDerivConverter = ArgumentChecker.notNull(defnToDerivConverter, "defnToDerivConverter");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "htsFn");
  }

  @Override
  public Result<Pair<SwapDefinition, InstrumentDerivative>> convert(Environment env, InterestRateSwapSecurity security) {
    Result<HistoricalTimeSeriesBundle> fixingsResult = _fixingsFn.getFixingsForSecurity(env, security);

    if (!fixingsResult.isSuccess()) {
      return Result.failure(fixingsResult);
    }
    HistoricalTimeSeriesBundle fixings = fixingsResult.getValue();
    SwapDefinition definition = (SwapDefinition) security.accept(_secToDefnConverter);
    InstrumentDerivative derivative = _defnToDerivConverter.convert(security, definition, env.getValuationTime(), fixings);
    s_logger.debug("Created definition {} and derivative {} for security {}", definition, derivative, security);
    return Result.success(Pairs.of(definition, derivative));
  }
}
