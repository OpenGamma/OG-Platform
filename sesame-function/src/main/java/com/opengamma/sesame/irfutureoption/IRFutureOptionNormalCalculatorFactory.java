package com.opengamma.sesame.irfutureoption;

import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
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

    IRFutureOptionSecurity security = trade.getSecurity();
    Result<MulticurveBundle> multicurveBundle = _multicurveFn.getMulticurveBundle(env, trade);
    Result<VolatilitySurface> surfaceResult =
        env.getMarketDataBundle().get(VolatilitySurfaceId.of(_volSurfaceName), VolatilitySurface.class);
    Result<HistoricalTimeSeriesBundle> fixingsResult = _fixingsFn.getFixingsForSecurity(env, security);

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
    NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalMulticurve =
        new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(bundle.getMulticurveProvider(),
                                                                volSurface.getSurface(),
                                                                underlyingFuture.getIborIndex(),
                                                                _moneynessOnPrice);

    Set<String> curveNames = normalMulticurve.getMulticurveProvider().getAllCurveNames();
    Result<Map<String, CurveMatrixLabeller>> curveLabellers = _curveLabellingFn.getCurveLabellers(curveNames);

    if (Result.anyFailures(curveLabellers)) {
     return Result.failure(curveLabellers);
    }

    IRFutureOptionCalculator calculator = new IRFutureOptionNormalCalculator(derivative,
                                                                             normalMulticurve,
                                                                             curveLabellers.getValue());
    return Result.success(calculator);

  }

}
