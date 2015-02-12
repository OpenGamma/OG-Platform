package com.opengamma.sesame.irfutureoption;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.businessday.ModifiedFollowingBusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.DoublesPair;

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
  private final GridInterpolator2D _interpolator;

  public IRFutureOptionNormalCalculatorFactory(InterestRateFutureOptionTradeConverter converter,
                                               FixedIncomeConverterDataProvider definitionToDerivativeConverter,
                                               DiscountingMulticurveCombinerFn multicurveFn,
                                               FixingsFn fixingsFn,
                                               CurveLabellingFn curveLabellingFn,
                                               boolean moneynessOnPrice,
                                               GridInterpolator2D interpolator) {
    _converter = ArgumentChecker.notNull(converter, "converter");
    _definitionToDerivativeConverter =
        ArgumentChecker.notNull(definitionToDerivativeConverter, "definitionToDerivativeConverter");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "fixingsFn");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
    //TODO is it right to wire this in here?
    _multicurveFn = ArgumentChecker.notNull(multicurveFn, "multicurveFn");
    _moneynessOnPrice = ArgumentChecker.notNull(moneynessOnPrice, "moneynessOnPrice");
    _interpolator = ArgumentChecker.notNull(interpolator, "interpolator");
  }

  @Override
  public Result<IRFutureOptionCalculator> createCalculator(Environment env, IRFutureOptionTrade trade) {

    IRFutureOptionSecurity security = trade.getSecurity();
    Result<HistoricalTimeSeriesBundle> fixingsResult = _fixingsFn.getFixingsForSecurity(env, security);

    Result<MulticurveBundle> multicurveBundle = _multicurveFn.getMulticurveBundle(env, trade);

    if (!multicurveBundle.isSuccess()) {
      return Result.failure(multicurveBundle);
    }

    MulticurveBundle bundle = multicurveBundle.getValue();
    NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalMulticurve = createNormalMulticurve(security, bundle);
    Set<String> curveNames = normalMulticurve.getMulticurveProvider().getAllCurveNames();
    Result<Map<String, CurveMatrixLabeller>> curveLabellers = _curveLabellingFn.getCurveLabellers(curveNames);


    if (Result.anyFailures(fixingsResult, curveLabellers)) {
     return Result.failure(fixingsResult, curveLabellers);
    } else {


      IRFutureOptionCalculator calculator = new IRFutureOptionNormalCalculator(trade,
                                                                               _converter,
                                                                               normalMulticurve,
                                                                               env.getValuationTime(),
                                                                               _definitionToDerivativeConverter,
                                                                               fixingsResult.getValue(),
                                                                               curveLabellers.getValue());
      return Result.success(calculator);
    }

  }

  private NormalSTIRFuturesExpSimpleMoneynessProviderDiscount createNormalMulticurve(IRFutureOptionSecurity security,
                                                                                     MulticurveBundle bundle) {


    //TODO where does this come from?
    Map<DoublesPair, Double> vols = new HashMap<DoublesPair, Double>();
    InterpolatedDoublesSurface surface = new InterpolatedDoublesSurface(vols, _interpolator);
    //TODO where does this come from?
    IborIndex index = new IborIndex(security.getCurrency(),
                                    Period.ofMonths(3),
                                    0,
                                    DayCounts.ACT_360,
                                    new ModifiedFollowingBusinessDayConvention(),
                                    false,
                                    "");

    return new NormalSTIRFuturesExpSimpleMoneynessProviderDiscount(bundle.getMulticurveProvider(),
                                                                   surface,
                                                                   index,
                                                                   _moneynessOnPrice);

  }
}
