/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irfutureoption;

import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Normal calculator for interest rate future options.
 */
public class IRFutureOptionNormalCalculatorFactory implements IRFutureOptionCalculatorFactory {

  private final DiscountingMulticurveCombinerFn _multicurveFn;
  private final InterestRateFutureOptionTradeConverter _converter;
  private final FixedIncomeConverterDataProvider _definitionToDerivativeConverter;
  private final FixingsFn _fixingsFn;
  private final CurveLabellingFn _curveLabellingFn;
  private final IRFutureOptionNormalSurfaceProviderFn _surfaceProviderFn;
  private static final Result<HistoricalTimeSeriesBundle> EMPTY_BUNDLE = Result.success(new HistoricalTimeSeriesBundle());

  /**
   * Constructs a calculator factory for interest rate future options that will create a Normal calculator.
   * @param converter converter used to create the definition of the interest rate future option, not null.
   * @param definitionToDerivativeConverter converter used to create the derivative of the future option, not null.
   * @param fixingsFn function used to retrieve the historical prices of the underlying interest rate future.
   * @param curveLabellingFn function used to retrieve curve labellers for the multicurve
   * @param multicurveFn function used to retrieve the multicurve bundle
   */
  public IRFutureOptionNormalCalculatorFactory(InterestRateFutureOptionTradeConverter converter,
                                               FixedIncomeConverterDataProvider definitionToDerivativeConverter,
                                               FixingsFn fixingsFn,
                                               CurveLabellingFn curveLabellingFn,
                                               IRFutureOptionNormalSurfaceProviderFn surfaceProviderFn,
                                               DiscountingMulticurveCombinerFn multicurveFn) {
    _converter = ArgumentChecker.notNull(converter, "converter");
    _surfaceProviderFn =  ArgumentChecker.notNull(surfaceProviderFn, "surfaceProviderFn");
    _definitionToDerivativeConverter =
        ArgumentChecker.notNull(definitionToDerivativeConverter, "definitionToDerivativeConverter");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "fixingsFn");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
    _multicurveFn = ArgumentChecker.notNull(multicurveFn, "multicurveFn");
  }

  @Override
  public Result<IRFutureOptionCalculator> createCalculator(Environment env, IRFutureOptionTrade trade) {

    Result<MulticurveBundle> multicurveBundle = _multicurveFn.getMulticurveBundle(env, trade);
    Result<HistoricalTimeSeriesBundle> fixingsResult = getTimeSeries(env, trade);

    if (Result.anyFailures(multicurveBundle, fixingsResult)) {
      return Result.failure(multicurveBundle,  fixingsResult);
    }

    FuturesTransaction<InterestRateFutureOptionSecurity> derivative =
        IRFutureOptionFnUtils.createDerivative(trade,
                                               _converter,
                                               env.getValuationTime(),
                                               _definitionToDerivativeConverter,
                                               fixingsResult.getValue());

    InterestRateFutureSecurity underlyingFuture = derivative.getUnderlyingSecurity().getUnderlyingFuture();
    Result<NormalSTIRFuturesExpSimpleMoneynessProviderDiscount> normalSurfaceProvider =
        _surfaceProviderFn.getNormalSurfaceProvider(env, trade, underlyingFuture, multicurveBundle.getValue());

    if (!normalSurfaceProvider.isSuccess()) {
      return Result.failure(normalSurfaceProvider);
    }
    NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalSurface = normalSurfaceProvider.getValue();
    Set<String> curveNames = normalSurface.getMulticurveProvider().getAllCurveNames();
    Result<Map<String, CurveMatrixLabeller>> curveLabellers = _curveLabellingFn.getCurveLabellers(curveNames);

    if (Result.anyFailures(curveLabellers)) {
      return Result.failure(curveLabellers);
    }

    IRFutureOptionCalculator calculator = getCalculator(trade, derivative, normalSurface, curveLabellers.getValue());

    return Result.success(calculator);

  }

  /**
   * Create an instance of a IRFutureOptionCalculator, can be overwritten to return alternative calculators
   *
   * @param trade the trade to be priced (not used in default implementation)
   * @param derivative FuturesTransaction for InterestRateFutureOptionSecurity
   * @param normalSurface the normal surface provider
   * @param curveLabellers curve labellers for the multicurve
   *
   * @return IRFutureOptionCalculator, in this instance an IRFutureOptionNormalCalculator
   */
  protected IRFutureOptionCalculator getCalculator(IRFutureOptionTrade trade,
                                                   FuturesTransaction<InterestRateFutureOptionSecurity> derivative,
                                                   NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalSurface,
                                                   Map<String, CurveMatrixLabeller> curveLabellers) {
    return new IRFutureOptionNormalCalculator(derivative, normalSurface, curveLabellers);
  }

  private Result<HistoricalTimeSeriesBundle> getTimeSeries(Environment env, IRFutureOptionTrade trade) {
    if (IRFutureOptionFnUtils.requiresTimeSeries(env.getValuationDate(), trade)) {
      return _fixingsFn.getFixingsForSecurity(env, trade.getSecurity());
    } else {
      return EMPTY_BUNDLE;
    }

  }

}
