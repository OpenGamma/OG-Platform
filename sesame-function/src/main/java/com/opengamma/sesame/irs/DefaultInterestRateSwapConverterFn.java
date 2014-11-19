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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Converts an {@link InterestRateSwapSecurity} into an {@link InstrumentDefinition} and {@link InstrumentDerivative}.
 */
public class DefaultInterestRateSwapConverterFn implements InterestRateSwapConverterFn {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultInterestRateSwapConverterFn.class);

  private final InterestRateSwapSecurityConverter _secToDefnConverter;
  private final FixedIncomeConverterDataProvider _defnToDerivConverter;

  /**
   * @param secToDefnConverter converts an {@link InterestRateSwapSecurity} to a {@link SwapDefinition}
   * @param defnToDerivConverter converts a {@link SwapDefinition} to a {@link InstrumentDerivative}
   */
  public DefaultInterestRateSwapConverterFn(InterestRateSwapSecurityConverter secToDefnConverter,
                                            FixedIncomeConverterDataProvider defnToDerivConverter) {
    _secToDefnConverter = ArgumentChecker.notNull(secToDefnConverter, "secToDefnConverter");
    _defnToDerivConverter = ArgumentChecker.notNull(defnToDerivConverter, "defnToDerivConverter");
  }

  @Override
  public Result<SwapDefinition> createDefinition(Environment env, InterestRateSwapSecurity security) {
    SwapDefinition swapDefinition = (SwapDefinition) security.accept(_secToDefnConverter);
    s_logger.debug("Created definition {} for security {}", swapDefinition, security);
    return Result.success(swapDefinition);
  }

  @Override
  public Result<InstrumentDerivative> createDerivative(Environment env,
                                                       InterestRateSwapSecurity security,
                                                       SwapDefinition definition,
                                                       HistoricalTimeSeriesBundle fixings) {
    InstrumentDerivative derivative = _defnToDerivConverter.convert(security, definition, env.getValuationTime(), fixings);
    s_logger.debug("Created derivative {} for security {}", derivative, security);
    return Result.success(derivative);
  }

}
