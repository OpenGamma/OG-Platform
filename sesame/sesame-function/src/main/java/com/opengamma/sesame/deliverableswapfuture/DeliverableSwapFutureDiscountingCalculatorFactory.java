/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.deliverableswapfuture;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.conversion.DeliverableSwapFutureTradeConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
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
   * Function for fixings
   */
  private final FixingsFn _fixingsFn;

  /**
  * Curve labelling function
  */
  private final CurveLabellingFn _curveLabellingFn;

  private static final Result<HistoricalTimeSeriesBundle> EMPTY_BUNDLE = Result.success(new HistoricalTimeSeriesBundle());

/**
 * Constructs a discounting calculator factory for deliverable swap futures.
 *
 * @param deliverableSwapFutureTradeConverter the converter used to convert the OG-Financial deliverable swap future to
 *    the OG-Analytic definition.
 * @param definitionToDerivativeConverter the converter used to convert the definition to a derivative.
 * @param discountingMultiCurveCombinerFn the multicurve function.
 * @param fixingsFn function for looking up security fixings
 * @param curveLabellingFn function that provides curve labels
 */

  public DeliverableSwapFutureDiscountingCalculatorFactory(DeliverableSwapFutureTradeConverter deliverableSwapFutureTradeConverter,
                                                         FixedIncomeConverterDataProvider definitionToDerivativeConverter,
                                                         DiscountingMulticurveCombinerFn discountingMultiCurveCombinerFn,
                                                         FixingsFn fixingsFn,
                                                         CurveLabellingFn curveLabellingFn) {
  _deliverableSwapFutureTradeConverter =
      ArgumentChecker.notNull(deliverableSwapFutureTradeConverter, "deliverableSwapFutureTradeConverter");
  _definitionToDerivativeConverter =
      ArgumentChecker.notNull(definitionToDerivativeConverter, "definitionToDerivativeConverter");
  _discountingMultiCurveCombinerFn =
      ArgumentChecker.notNull(discountingMultiCurveCombinerFn, "discountingMultiCurveCombinerFn");
  _fixingsFn = ArgumentChecker.notNull(fixingsFn, "fixingsFn");
  _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
}



  @Override
  public Result<DeliverableSwapFutureCalculator> createCalculator(Environment env, DeliverableSwapFutureTrade trade) {

    Result<MulticurveBundle> bundleResult = _discountingMultiCurveCombinerFn.getMulticurveBundle(env, trade);

    Result<HistoricalTimeSeriesBundle> fixings = getTimeSeries(env, trade);
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

  /**
   * Is a time series of margin prices required. Not required if valued on trade date
   *
   * @param valuationDate the valuation date
   * @param trade the trade date
   * @return true if required, else false
   */
  private static boolean requiresTimeSeries(LocalDate valuationDate, DeliverableSwapFutureTrade trade) {
    return !valuationDate.equals(trade.getTrade().getTradeDate());
  }

  private Result<HistoricalTimeSeriesBundle> getTimeSeries(Environment env, DeliverableSwapFutureTrade trade) {
    if (requiresTimeSeries(env.getValuationDate(), trade)) {
      return _fixingsFn.getFixingsForSecurity(env, trade.getSecurity());
    } else {
      return EMPTY_BUNDLE;
    }
  }

}
