/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irfutureoption;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Black - Log-Normal calculator for interest rate future options.
 */
public class IRFutureOptionBlackCalculatorFactory implements IRFutureOptionCalculatorFactory {

  private static final Result<HistoricalTimeSeriesBundle> EMPTY_BUNDLE = Result.success(new HistoricalTimeSeriesBundle());

  /**
   * Converter used to create definition of the interest rate future option.
   */
  private final InterestRateFutureOptionTradeConverter _converter;

  /**
   * Function used to generate a Black volatility provider.
   */
  private final BlackSTIRFuturesProviderFn _blackProviderFn;

  /**
   * Converter used to create a definition from an interest rate future option.
   */
  private final FixedIncomeConverterDataProvider _definitionToDerivativeConverter;

  /**
   * Function used to retrieve the historical prices of the underlying interest rate future.
   */
  private final FixingsFn _fixingsFn;

  /**
   * Function used to retrieve the curve labellers for curves in a multicurve bundle.
   */
  private final CurveLabellingFn _curveLabellingFn;

  /**
   * Constructs a calculator factory for interest rate future options that will create a Black calculator.
   * @param converter converter used to create the definition of the interest rate future option, not null.
   * @param blackProviderFn function used to generate a Black volatility provider, not null.
   * @param definitionToDerivativeConverter converter used to create the derivative of the future option, not null.
   * @param fixingsFn function used to retrieve the historical prices of the underlying interest rate future.
   * @param curveLabellingFn function used to retrieve curve labellers for the multicurve
   */
  public IRFutureOptionBlackCalculatorFactory(InterestRateFutureOptionTradeConverter converter,
                                              BlackSTIRFuturesProviderFn blackProviderFn,
                                              FixedIncomeConverterDataProvider definitionToDerivativeConverter,
                                              FixingsFn fixingsFn,
                                              CurveLabellingFn curveLabellingFn) {
    _converter = ArgumentChecker.notNull(converter, "converter");
    _blackProviderFn = ArgumentChecker.notNull(blackProviderFn, "blackProviderFn");
    _definitionToDerivativeConverter =
        ArgumentChecker.notNull(definitionToDerivativeConverter, "definitionToDerivativeConverter");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "fixingsFn");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
  }

  @Override
  public Result<IRFutureOptionCalculator> createCalculator(Environment env, IRFutureOptionTrade trade) {

    Result<BlackSTIRFuturesProviderInterface> blackResult = _blackProviderFn.getBlackSTIRFuturesProvider(env, trade);
    Result<HistoricalTimeSeriesBundle> fixingsResult = getTimeSeries(env, trade);

    if (Result.anyFailures(blackResult, fixingsResult)) {
      return Result.failure(blackResult, fixingsResult);
    } else {
      BlackSTIRFuturesProviderInterface black = blackResult.getValue();
      Set<String> curveNames = black.getMulticurveProvider().getAllCurveNames();

      Result<Map<String, CurveMatrixLabeller>> curveLabellers = _curveLabellingFn.getCurveLabellers(curveNames);

      if (curveLabellers.isSuccess()) {
        IRFutureOptionCalculator calculator = new IRFutureOptionBlackCalculator(
            trade,
            _converter,
            black,
            env.getValuationTime(),
            _definitionToDerivativeConverter,
            fixingsResult.getValue(),
            curveLabellers.getValue());
        return Result.success(calculator);
      } else {
        return Result.failure(curveLabellers);
      }
    }

  }

  private Result<HistoricalTimeSeriesBundle> getTimeSeries(Environment env, IRFutureOptionTrade trade) {
    if (IRFutureOptionFnUtils.requiresTimeSeries(env.getValuationDate(), trade)) {
      return _fixingsFn.getFixingsForSecurity(env, trade.getSecurity());
    } else {
      return EMPTY_BUNDLE;
    }
  }
}
