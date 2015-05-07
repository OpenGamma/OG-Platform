/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.inflation;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InflationSwapSecurityConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.cache.CacheKey;
import com.opengamma.sesame.cache.FunctionCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Converts an {@link InterestRateSwapSecurity} into an {@link InstrumentDefinition} and {@link InstrumentDerivative}.
 */
public class DefaultInflationSwapConverterFn implements InflationSwapConverterFn {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultInflationSwapConverterFn.class);

  private final InflationSwapSecurityConverter _secToDefnConverter;
  private final FixedIncomeConverterDataProvider _defnToDerivConverter;
  private final FixingsFn _fixingsFn;
  private final FunctionCache _cache;

  /**
   * @param secToDefnConverter converts an {@link ZeroCouponInflationSwapSecurity} to a {@link SwapDefinition}
   * @param defnToDerivConverter converts a {@link SwapDefinition} to a {@link InstrumentDerivative}
   * @param fixingsFn provides time series of fixings for the security
   * @param cache for caching definitions and derivatives
   */
  public DefaultInflationSwapConverterFn(InflationSwapSecurityConverter secToDefnConverter,
                                         FixedIncomeConverterDataProvider defnToDerivConverter,
                                         FixingsFn fixingsFn,
                                         FunctionCache cache) {
    _cache = ArgumentChecker.notNull(cache, "functionCache");
    _secToDefnConverter = ArgumentChecker.notNull(secToDefnConverter, "secToDefnConverter");
    _defnToDerivConverter = ArgumentChecker.notNull(defnToDerivConverter, "defnToDerivConverter");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "htsFn");
  }

  @Override
  public Result<Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative>> convert(final Environment env, final ZeroCouponInflationSwapSecurity security) {
    Result<HistoricalTimeSeriesBundle> fixingsResult = _fixingsFn.getFixingsForSecurity(env, security);

    if (!fixingsResult.isSuccess()) {
      return Result.failure(fixingsResult);
    }

    final HistoricalTimeSeriesBundle fixings = fixingsResult.getValue();

    CacheKey key = CacheKey.of(this, env.getValuationTime(), security);
    Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative> pair = _cache.get(
        key, new Callable<Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative>>() {
          @Override
          public Pair<SwapFixedInflationZeroCouponDefinition, InstrumentDerivative> call() throws Exception {
            SwapFixedInflationZeroCouponDefinition definition = (SwapFixedInflationZeroCouponDefinition) security.accept(_secToDefnConverter);
            InstrumentDerivative derivative =
                _defnToDerivConverter.convert(security, definition, env.getValuationTime(), fixings);
            return Pairs.of(definition, derivative);
          }
        });
    s_logger.debug(
        "Created definition {} and derivative {} for security {}",
        pair.getFirst(),
        pair.getSecond(),
        security);
    return Result.success(pair);
  }
}
