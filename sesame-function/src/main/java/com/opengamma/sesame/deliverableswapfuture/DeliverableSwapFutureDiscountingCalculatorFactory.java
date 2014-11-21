/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.deliverableswapfuture;

import java.util.Map;
import java.util.Set;

import com.opengamma.financial.analytics.conversion.DeliverableSwapFutureTradeConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.HistoricalTimeSeriesFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.DeliverableSwapFutureTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Default factory for deliverable swap future calculators that provides the converter used to convert the security to an
 * OG-Analytics representation.
 */
public class DeliverableSwapFutureDiscountingCalculatorFactory implements DeliverableSwapFutureCalculatorFactory {

  /**
   * Converter for a deliverable swap future trade
   */
  private final DeliverableSwapFutureTradeConverter _deliverableSwapFutureTradeConverter;
  
  /**
   * Definition to derivative converter
   */
  private final FixedIncomeConverterDataProvider _definitionToDerivativeConverter;
  
  /**
   * Function used to generate a combined multicurve bundle suitable
   * for use with a particular security.
   */
  private final DiscountingMulticurveCombinerFn _discountingMultiCurveCombinerFn;
  
  /**
   * HTS function for fixings
   */
  private final HistoricalTimeSeriesFn _htsFn;

  /**
  * Curve labelling function
  */
  private final CurveLabellingFn _curveLabellingFn;

  /**
   * Constructs a discounting calculator factory for deliverable swap futures.
   *
   * @param deliverableSwapFutureTradeConverter the converter used to convert the OG-Financial deliverable swap future to
   *    the OG-Analytic definition.
   * @param definitionToDerivativeConverter the converter used to convert the definition to a derivative.
   * @param discountingMultiCurveCombinerFn the multicurve function.
   * @param htsFn the historical time series function
   * @param curveLabellingFn the curve labelling function
   */
  public DeliverableSwapFutureDiscountingCalculatorFactory(DeliverableSwapFutureTradeConverter deliverableSwapFutureTradeConverter,
                                                           FixedIncomeConverterDataProvider definitionToDerivativeConverter,
                                                           DiscountingMulticurveCombinerFn discountingMultiCurveCombinerFn,
                                                           HistoricalTimeSeriesFn htsFn,
                                                           CurveLabellingFn curveLabellingFn) {
    _deliverableSwapFutureTradeConverter =
        ArgumentChecker.notNull(deliverableSwapFutureTradeConverter, "deliverableSwapFutureTradeConverter");
    _definitionToDerivativeConverter =
        ArgumentChecker.notNull(definitionToDerivativeConverter, "definitionToDerivativeConverter");
    _discountingMultiCurveCombinerFn =
        ArgumentChecker.notNull(discountingMultiCurveCombinerFn, "discountingMultiCurveCombinerFn");
    _htsFn = ArgumentChecker.notNull(htsFn, "htsFn");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
  }

  @Override
  public Result<DeliverableSwapFutureCalculator> createCalculator(Environment env, DeliverableSwapFutureTrade trade) {

    DeliverableSwapFutureSecurity security = trade.getSecurity();

    Result<MulticurveBundle> bundleResult = _discountingMultiCurveCombinerFn.getMulticurveBundle(env, trade);
    Result<HistoricalTimeSeriesBundle> fixings = _htsFn.getFixingsForSecurity(env, security);

    if (Result.anyFailures(bundleResult, fixings)) {
      return Result.failure(bundleResult, fixings);
    } else {

      MulticurveBundle multicurveBundle = bundleResult.getValue();

      Set<String> curveNames = multicurveBundle.getCurveBuildingBlockBundle().getData().keySet();
      Result<Map<String, CurveMatrixLabeller>> curveLabellers = _curveLabellingFn.getCurveLabellers(curveNames);

      if (curveLabellers.isSuccess()) {
        DeliverableSwapFutureCalculator calculator =
            new DeliverableSwapFutureDiscountingCalculator(
                trade,
                multicurveBundle.getMulticurveProvider(),
                _deliverableSwapFutureTradeConverter,
                env.getValuationTime(),
                _definitionToDerivativeConverter,
                fixings.getValue(),
                curveLabellers.getValue());
        return Result.success(calculator);
      } else {
        return Result.failure(curveLabellers);
      }
    }
  }

}
