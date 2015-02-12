package com.opengamma.sesame.irfutureoption;

import java.util.Map;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionSecurity;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionDeltaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionGammaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionThetaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PositionVegaNormalSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueCurveSensitivityNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalstirfutures.PresentValueNormalSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesExpSimpleMoneynessProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.ZeroIRDeltaBucketingUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;

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
  private static final PositionDeltaNormalSTIRFutureOptionCalculator DELTA_CALC =
      PositionDeltaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionGammaNormalSTIRFutureOptionCalculator GAMMA_CALC =
      PositionGammaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionThetaNormalSTIRFutureOptionCalculator THETA_CALC =
      PositionThetaNormalSTIRFutureOptionCalculator.getInstance();
  private static final PositionVegaNormalSTIRFutureOptionCalculator VEGA_CALC =
      PositionVegaNormalSTIRFutureOptionCalculator.getInstance();

  private final FuturesTransaction<InterestRateFutureOptionSecurity> _derivative;
  private final Map<String, CurveMatrixLabeller> _curveLabellers;
  private final NormalSTIRFuturesExpSimpleMoneynessProviderDiscount _normalMulticurve;

  public IRFutureOptionNormalCalculator(FuturesTransaction<InterestRateFutureOptionSecurity> derivative,
                                        NormalSTIRFuturesExpSimpleMoneynessProviderDiscount normalMulticurve,
                                        Map<String, CurveMatrixLabeller> curveLabellers) {
    _normalMulticurve = ArgumentChecker.notNull(normalMulticurve, "normalMulticurve");
    _curveLabellers = ArgumentChecker.notNull(curveLabellers, "curveLabellers");
    _derivative = ArgumentChecker.notNull(derivative, "derivative");
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePV() {
    return Result.success(_derivative.accept(PV_CALC, _normalMulticurve));
  }

  @Override
  public Result<MultipleCurrencyMulticurveSensitivity> calculatePV01() {
    return Result.success(_derivative.accept(PV01_CALC, _normalMulticurve));
  }

  @Override
  public Result<Double> calculateModelPrice() {
    return Result.success(_derivative.accept(PRICE_CALC, _normalMulticurve));
  }

  @Override
  public Result<Double> calculateDelta() {
    return Result.success(_derivative.accept(DELTA_CALC, _normalMulticurve));
  }

  @Override
  public Result<Double> calculateGamma() {
    return Result.success(_derivative.accept(GAMMA_CALC, _normalMulticurve));
  }

  @Override
  public Result<Double> calculateVega() {
    return Result.success(_derivative.accept(VEGA_CALC, _normalMulticurve));
  }

  @Override
  public Result<Double> calculateTheta() {
    return Result.success(_derivative.accept(THETA_CALC, _normalMulticurve));
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedZeroIRDelta() {
    MultipleCurrencyParameterSensitivity sensitivities = PSSFC.calculateSensitivity(_derivative, _normalMulticurve);
    BucketedCurveSensitivities bucketedCurveSensitivities =
        ZeroIRDeltaBucketingUtils.getBucketedCurveSensitivities(sensitivities, _curveLabellers);

    return Result.success(bucketedCurveSensitivities);
  }
}
