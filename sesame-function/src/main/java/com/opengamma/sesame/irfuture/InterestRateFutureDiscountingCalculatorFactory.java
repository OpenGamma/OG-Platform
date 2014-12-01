/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irfuture;

import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.InterestRateFutureTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.InterestRateFutureTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Default factory for interest rate future calculators that provides the converter used to convert the security to an
 * OG-Analytics representation.
 */
public class InterestRateFutureDiscountingCalculatorFactory implements InterestRateFutureCalculatorFactory {

  private final InterestRateFutureTradeConverter _converter;

  private final FixedIncomeConverterDataProvider _definitionToDerivativeConverter;

  private final DiscountingMulticurveCombinerFn _discountingMulticurveCombinerFn;

  private final FixingsFn _fixingsFn;

  private final CurveLabellingFn _curveLabellingFn;

  /**
   * Constructs a factory that creates discounting calculators for STIR futures.
   *
   * @param converter the converter used to convert the OG-Financial STIR future to the OG-Analytics definition.
   * @param definitionToDerivativeConverter the converter used to convert the definition to derivative.
   * @param discountingMulticurveCombinerFn the multicurve function.
   * @param curveLabellingFn the curve labelling function.
   * @param fixingsFn for looking up fixings
   */
  public InterestRateFutureDiscountingCalculatorFactory(
      InterestRateFutureTradeConverter converter,
      FixedIncomeConverterDataProvider definitionToDerivativeConverter,
      DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn,
      CurveLabellingFn curveLabellingFn,
      FixingsFn fixingsFn) {

    _converter = ArgumentChecker.notNull(converter, "converter");
    _definitionToDerivativeConverter =
        ArgumentChecker.notNull(definitionToDerivativeConverter, "definitionToDerivativeConverter");
    _discountingMulticurveCombinerFn = 
        ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
    _fixingsFn = ArgumentChecker.notNull(fixingsFn, "htsFn");
  }

  @Override
  public Result<InterestRateFutureCalculator> createCalculator(Environment env, InterestRateFutureTrade trade) {

    FinancialSecurity security = trade.getSecurity();

    Result<MulticurveBundle> bundleResult = _discountingMulticurveCombinerFn.getMulticurveBundle(env, trade);
    Result<HistoricalTimeSeriesBundle> fixingsResult = _fixingsFn.getFixingsForSecurity(env, security);

    if (Result.allSuccessful(bundleResult, fixingsResult)) {

      MulticurveProviderDiscount multicurveBundle = bundleResult.getValue().getMulticurveProvider();

      CurveBuildingBlockBundle buildingBlockBundle = bundleResult.getValue().getCurveBuildingBlockBundle();
      Set<String> curveNames = buildingBlockBundle.getData().keySet();
      Result<Map<String, CurveMatrixLabeller>> curveLabellers = _curveLabellingFn.getCurveLabellers(curveNames);

      if (curveLabellers.isSuccess()) {
        InterestRateFutureCalculator calculator =
            new InterestRateFutureDiscountingCalculator(
                trade,
                multicurveBundle,
                curveLabellers.getValue(),
                _converter,
                env.getValuationTime(),
                _definitionToDerivativeConverter,
                fixingsResult.getValue());

        return Result.success(calculator);
      } else {
        return Result.failure(curveLabellers);
      }
    } else {
      return Result.failure(bundleResult, fixingsResult);
    }
  }

}
