package com.opengamma.sesame.irfutureoption;

import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceMulticurveCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionDeltaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionGammaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionThetaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionVegaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueCurveSensitivityNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.ZeroIRDeltaBucketingUtils;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Interest rate future option Normal calculator.
 */
public class IRFutureOptionNormalCalculator implements IRFutureOptionCalculator {

  private static final FuturesPriceNormalSTIRFuturesCalculator PRICE_CALC =
      FuturesPriceNormalSTIRFuturesCalculator.getInstance();
  private static final PresentValueNormalSTIRFuturesCalculator PV_CALC =
      PresentValueNormalSTIRFuturesCalculator.getInstance();
  private static final PresentValueCurveSensitivityNormalSTIRFuturesCalculator PV01_CALC =
      PresentValueCurveSensitivityNormalSTIRFuturesCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<NormalSTIRFuturesProviderInterface> PSSFC =
      new ParameterSensitivityParameterCalculator<>(PV01_CALC);
  //private static final PV01CurveParametersCalculator<NormalSTIRFuturesProviderInterface> PV01_CALC =
  //    new PV01CurveParametersCalculator<>(PVSC);
  private static final PositionDeltaNormalSTIRFutureOptionCalculator DELTA_CALC =
      PositionDeltaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionGammaNormalSTIRFutureOptionCalculator GAMMA_CALC =
      PositionGammaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionThetaNormalSTIRFutureOptionCalculator THETA_CALC =
      PositionThetaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionVegaNormalSTIRFutureOptionCalculator VEGA_CALC =
      PositionVegaNormalSTIRFutureOptionCalculator.getInstance();
  private static final FuturesPriceMulticurveCalculator FPC =
      FuturesPriceMulticurveCalculator.getInstance();

  private final InstrumentDerivative _derivative;
  private final Map<String, CurveMatrixLabeller> _curveLabellers;
  private final NormalSTIRFuturesExpSimpleMoneynessProviderDiscount _normalMulticurves;


  public IRFutureOptionNormalCalculator(IRFutureOptionTrade trade,
                                        InterestRateFutureOptionTradeConverter converter,
                                        NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalMulticurves,
                                        ZonedDateTime valTime,
                                        FixedIncomeConverterDataProvider definitionToDerivativeConverter,
                                        HistoricalTimeSeriesBundle fixings,
                                        Map<String, CurveMatrixLabeller> curveLabellers) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.notNull(converter, "converter");
    ArgumentChecker.notNull(valTime, "valTime");
    ArgumentChecker.notNull(definitionToDerivativeConverter, "definitionToDerivativeConverter");
    ArgumentChecker.notNull(fixings, "fixings");

    _normalMulticurves = ArgumentChecker.notNull(normalMulticurves, "normalMulticurves");
    _curveLabellers = ArgumentChecker.notNull(curveLabellers, "curveLabellers");
    _derivative = IRFutureOptionFnUtils.createDerivative(trade,
                                                         converter,
                                                         valTime,
                                                         definitionToDerivativeConverter,
                                                         fixings);
  }


  @Override
  public Result<MultipleCurrencyAmount> calculatePV() {
    return Result.success(_derivative.accept(PV_CALC, _normalMulticurves));
  }

  @Override
  public Result<MultipleCurrencyMulticurveSensitivity> calculatePV01() {
    return Result.success(_derivative.accept(PV01_CALC, _normalMulticurves));
  }

  @Override
  public Result<Double> calculateModelPrice() {
    return Result.success(_derivative.accept(PRICE_CALC, _normalMulticurves));
  }

  @Override
  public Result<Double> calculateDelta() {
    return Result.success(_derivative.accept(DELTA_CALC, _normalMulticurves));
  }

  @Override
  public Result<Double> calculateGamma() {
    return Result.success(_derivative.accept(GAMMA_CALC, _normalMulticurves));
  }

  @Override
  public Result<Double> calculateVega() {
    return Result.success(_derivative.accept(VEGA_CALC, _normalMulticurves));
  }

  @Override
  public Result<Double> calculateTheta() {
    return Result.success(_derivative.accept(THETA_CALC, _normalMulticurves));
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedZeroIRDelta() {
    MultipleCurrencyParameterSensitivity sensitivities = PSSFC.calculateSensitivity(_derivative, _normalMulticurves);
    BucketedCurveSensitivities bucketedCurveSensitivities =
        ZeroIRDeltaBucketingUtils.getBucketedCurveSensitivities(sensitivities, _curveLabellers);

    return Result.success(bucketedCurveSensitivities);
  }
}
