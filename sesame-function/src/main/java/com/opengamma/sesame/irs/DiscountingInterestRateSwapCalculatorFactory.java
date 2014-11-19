/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irs;

import java.math.BigDecimal;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveDefinitionFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.HistoricalTimeSeriesFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Factory class for creating a calculator for a discounting swap.
 */
public class DiscountingInterestRateSwapCalculatorFactory implements InterestRateSwapCalculatorFactory {

  /**
   * Function used to generate a combined multicurve bundle suitable
   * for use with a particular security.
   */
  private final DiscountingMulticurveCombinerFn _discountingMulticurveCombinerFn;

  /**
   * HTS function for fixings
   */
  private final HistoricalTimeSeriesFn _htsFn;

  /**
   * Curve definition function
   */
  private final CurveDefinitionFn _curveDefinitionFn;

  /**
   * Converts the swap into a definition and derivative
   */
  private final InterestRateSwapConverterFn _converterFn;

  /**
   * Creates the factory.
   *
   * @param discountingMulticurveCombinerFn function for creating multicurve bundles, not null
   * @param htsFn function for providing fixing timeseries, not null.
   * @param curveDefinitionFn the curve definition function, not null.
   * @param converterFn converts the swap into a definition and derivative
   */
  public DiscountingInterestRateSwapCalculatorFactory(DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn,
                                                      HistoricalTimeSeriesFn htsFn,
                                                      CurveDefinitionFn curveDefinitionFn,
                                                      InterestRateSwapConverterFn converterFn) {
    _discountingMulticurveCombinerFn = ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
    _htsFn = ArgumentChecker.notNull(htsFn, "htsFn");
    _curveDefinitionFn = ArgumentChecker.notNull(curveDefinitionFn, "curveDefinitionFn");
    _converterFn = ArgumentChecker.notNull(converterFn, "converterFn");
  }

  @Override
  public Result<InterestRateSwapCalculator> createCalculator(Environment env, InterestRateSwapSecurity security) {

    Trade trade = new SimpleTrade(security,
                                  BigDecimal.ONE,
                                  new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                  LocalDate.now(),
                                  OffsetTime.now());
    InterestRateSwapTrade tradeWrapper = new InterestRateSwapTrade(trade);
    return createCalculator(env, tradeWrapper);

  }

  @Override
  public Result<InterestRateSwapCalculator> createCalculator(Environment env, InterestRateSwapTrade trade) {

    Result<HistoricalTimeSeriesBundle> fixings = _htsFn.getFixingsForSecurity(env, trade.getSecurity());
    Result<MulticurveBundle> bundleResult = _discountingMulticurveCombinerFn.getMulticurveBundle(env, trade);
    Result<SwapDefinition> definitionResult = _converterFn.createDefinition(env, trade.getSecurity());

    if (Result.allSuccessful(bundleResult, fixings, definitionResult)) {

      Result<InstrumentDerivative> derivativeResult = _converterFn.createDerivative(env,
                                                                                    trade.getSecurity(),
                                                                                    definitionResult.getValue(),
                                                                                    fixings.getValue());

      Result<Map<String, CurveDefinition>> curveDefinitions =
          _curveDefinitionFn.getCurveDefinitions(bundleResult.getValue().getCurveBuildingBlockBundle().getData().keySet());

      if (Result.anyFailures(curveDefinitions, derivativeResult)) {
        return Result.failure(curveDefinitions, derivativeResult);
      }

      InterestRateSwapCalculator calculator =
          new DiscountingInterestRateSwapCalculator(trade,
                                                    bundleResult.getValue().getMulticurveProvider(),
                                                    bundleResult.getValue().getCurveBuildingBlockBundle(),
                                                    env.getValuationTime(),
                                                    curveDefinitions.getValue(),
                                                    definitionResult.getValue(),
                                                    derivativeResult.getValue());
      return Result.success(calculator);
    } else {
      return Result.failure(bundleResult, fixings);
    }
  }
}
