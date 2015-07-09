/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irs;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

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
   * Converts the swap into a definition and derivative
   */
  private final CurveLabellingFn _curveLabellingFn;

  /**
   * Converts the swap into a definition and derivative
   */
  private final InterestRateSwapConverterFn _converterFn;

  /**
   * Creates the factory.
   *
   * @param discountingMulticurveCombinerFn  function for creating multicurve bundles, not null
   * @param curveLabellingFn  the curve labelling function, not null.
   * @param converterFn  converts the swap into a definition and derivative
   */
  public DiscountingInterestRateSwapCalculatorFactory(
      DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn,
      CurveLabellingFn curveLabellingFn,
      InterestRateSwapConverterFn converterFn) {
    _discountingMulticurveCombinerFn = ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
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
    Result<MulticurveBundle> bundleResult = _discountingMulticurveCombinerFn.getMulticurveBundle(env, trade);
    Result<Pair<SwapDefinition, InstrumentDerivative>> convertResult = _converterFn.convert(env, trade.getSecurity());

    if (Result.allSuccessful(bundleResult, convertResult)) {

      Set<String> curveNames = bundleResult.getValue().getCurveBuildingBlockBundle().getData().keySet();
      Result<Map<String, CurveMatrixLabeller>> curveLabels =
          _curveLabellingFn.getCurveLabellers(curveNames);

      if (curveLabels.isSuccess()) {

        InterestRateSwapCalculator calculator =
            new DiscountingInterestRateSwapCalculator(
                trade,
                bundleResult.getValue().getMulticurveProvider(),
                bundleResult.getValue().getCurveBuildingBlockBundle(),
                env.getValuationTime(),
                curveLabels.getValue(),
                convertResult.getValue().getFirst(),
                convertResult.getValue().getSecond());
        return Result.success(calculator);
      } else {
        return Result.failure(curveLabels);
      }
    } else {
      return Result.failure(bundleResult, convertResult);
    }
  }
}
