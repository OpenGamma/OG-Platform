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
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
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
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Normal calculator for interest rate future options.
 */
public class IRFutureOptionNormalCalculatorFactory implements IRFutureOptionCalculatorFactory {

  private final InterestRateFutureOptionTradeConverter _converter;
  private final FixedIncomeConverterDataProvider _definitionToDerivativeConverter;
  private final FixingsFn _fixingsFn;
  private final CurveLabellingFn _curveLabellingFn;
  private final DiscountingMulticurveCombinerFn _multicurveFn;
  private final boolean _moneynessOnPrice;
  private final String _volSurfaceName;
  private static final Result<HistoricalTimeSeriesBundle> EMPTY_BUNDLE = Result.success(new HistoricalTimeSeriesBundle());

  /**
   * Constructs a calculator factory for interest rate future options that will create a Normal calculator.
   * @param converter converter used to create the definition of the interest rate future option, not null.
   * @param definitionToDerivativeConverter converter used to create the derivative of the future option, not null.
   * @param multicurveFn function used to retrieve the multicurve bundle
   * @param fixingsFn function used to retrieve the historical prices of the underlying interest rate future.
   * @param curveLabellingFn function used to retrieve curve labellers for the multicurve
   * @param moneynessOnPrice flag indicating if the moneyness is on the price (true) or on the rate (false).
   * @param volSurfaceName name of the volatility surface
   */
  public IRFutureOptionNormalCalculatorFactory(InterestRateFutureOptionTradeConverter converter,
                                               FixedIncomeConverterDataProvider definitionToDerivativeConverter,
                                               DiscountingMulticurveCombinerFn multicurveFn,
                                               FixingsFn fixingsFn,
                                               CurveLabellingFn curveLabellingFn,
                                               boolean moneynessOnPrice,
                                               String volSurfaceName) {
    _converter = ArgumentChecker.notNull(converter, "converter");
    _definitionToDerivativeConverter =
        ArgumentChecker.notNull(definitionToDerivativeConverter, "definitionToDerivativeConverter");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "fixingsFn");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
    _multicurveFn = ArgumentChecker.notNull(multicurveFn, "multicurveFn");
    _moneynessOnPrice = ArgumentChecker.notNull(moneynessOnPrice, "moneynessOnPrice");
    _volSurfaceName = ArgumentChecker.notNull(volSurfaceName, "volSurfaceName");
  }

  @Override
  public Result<IRFutureOptionCalculator> createCalculator(Environment env, IRFutureOptionTrade trade) {

    Result<MulticurveBundle> multicurveBundle = _multicurveFn.getMulticurveBundle(env, trade);
    Result<VolatilitySurface> surfaceResult =
        env.getMarketDataBundle().get(VolatilitySurfaceId.of(_volSurfaceName), VolatilitySurface.class);
    Result<HistoricalTimeSeriesBundle> fixingsResult = getTimeSeries(env, trade);

    if (Result.anyFailures(multicurveBundle, surfaceResult, fixingsResult)) {
      return Result.failure(multicurveBundle, surfaceResult, fixingsResult);
    }

    FuturesTransaction<InterestRateFutureOptionSecurity> derivative =
        IRFutureOptionFnUtils.createDerivative(trade,
                                               _converter,
                                               env.getValuationTime(),
                                               _definitionToDerivativeConverter,
                                               fixingsResult.getValue());

    VolatilitySurface volSurface = surfaceResult.getValue();
    MulticurveBundle bundle = multicurveBundle.getValue();

    InterestRateFutureSecurity underlyingFuture = derivative.getUnderlyingSecurity().getUnderlyingFuture();
    NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalSurface =
        new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(bundle.getMulticurveProvider(),
                                                                volSurface.getSurface(),
                                                                underlyingFuture.getIborIndex(),
                                                                _moneynessOnPrice);

    Set<String> curveNames = normalSurface.getMulticurveProvider().getAllCurveNames();
    Result<Map<String, CurveMatrixLabeller>> curveLabellers = _curveLabellingFn.getCurveLabellers(curveNames);

    if (Result.anyFailures(curveLabellers)) {
     return Result.failure(curveLabellers);
    }

    IRFutureOptionCalculator calculator = getCalculator(derivative, normalSurface, curveLabellers.getValue());

    return Result.success(calculator);

  }

  /**
   * Create an instance of a IRFutureOptionCalculator, can be overwritten to return alternative calculators
   *
   * @param derivative FuturesTransaction for InterestRateFutureOptionSecurity
   * @param normalSurface the normal surface provider
   * @param curveLabellers curve labellers for the multicurve
   *
   * @return IRFutureOptionCalculator, in this instance an IRFutureOptionNormalCalculator
   */
  protected IRFutureOptionCalculator getCalculator(FuturesTransaction<InterestRateFutureOptionSecurity> derivative,
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
