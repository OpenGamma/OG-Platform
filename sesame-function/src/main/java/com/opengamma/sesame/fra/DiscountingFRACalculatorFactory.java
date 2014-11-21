/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fra;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.HistoricalTimeSeriesFn;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.ForwardRateAgreementTrade;
import com.opengamma.sesame.trade.FraTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Factory class for creating a calculator for a discounting FRA.
 */
public class DiscountingFRACalculatorFactory implements FRACalculatorFactory {

  /**
   * Converter for a FRA.
   */
  private final FRASecurityConverter _fraConverter;

  /**
   * Definition to derivative converter.
   */
  private final FixedIncomeConverterDataProvider _fixedIncomeConverterDataProvider;

  /**
   * Function used to generate a combined multicurve bundle suitable
   * for use with a particular security.
   */
  private final DiscountingMulticurveCombinerFn _discountingMulticurveCombinerFn;

  /**
   * HTS function for fixings.
   */
  private final HistoricalTimeSeriesFn _htsFn;

  /**
   * Curve labelling function.
   */
  private final CurveLabellingFn _curveLabellingFn;

  /**
   * Creates the factory.
   *
   * @param fraConverter for transforming a fra into its InstrumentDefinition form, not null
   * @param fixedIncomeConverterDataProvider for transforming the InstrumentDefinition into Derivative, not null
   * @param discountingMulticurveCombinerFn function for creating multicurve bundles, not null
   * @param htsFn Function to get the fixing for a security, not null
   * @param curveLabellingFn the curve labelling function, not null.
   */
  public DiscountingFRACalculatorFactory(FRASecurityConverter fraConverter,
                                         FixedIncomeConverterDataProvider fixedIncomeConverterDataProvider,
                                         DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn,
                                         HistoricalTimeSeriesFn htsFn,
                                         CurveLabellingFn curveLabellingFn) {
    _fraConverter = ArgumentChecker.notNull(fraConverter, "fraConverter");
    _fixedIncomeConverterDataProvider = 
        ArgumentChecker.notNull(fixedIncomeConverterDataProvider, "fixedIncomeConverterDataProvider");
    _discountingMulticurveCombinerFn = 
        ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
    _htsFn = ArgumentChecker.notNull(htsFn, "htsFn");
    _curveLabellingFn = ArgumentChecker.notNull(curveLabellingFn, "curveLabellingFn");
  }

  @Override
  public Result<FRACalculator> createCalculator(Environment env, FRASecurity security) {

    Trade trade = new SimpleTrade(
        security,
        BigDecimal.ONE,
        new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
        LocalDate.now(),
        OffsetTime.now());
    FraTrade tradeWrapper = new FraTrade(trade);

    Result<MulticurveBundle> bundleResult = _discountingMulticurveCombinerFn.getMulticurveBundle(env, tradeWrapper);

    if (bundleResult.isSuccess()) {

      Set<String> curveNames = bundleResult.getValue().getCurveBuildingBlockBundle().getData().keySet();
      Result<Map<String, CurveMatrixLabeller>> curveLabellers =
          _curveLabellingFn.getCurveLabellers(curveNames);

      if (curveLabellers.isSuccess()) {
        FRACalculator calculator = new DiscountingFRACalculator(security,
            bundleResult.getValue().getMulticurveProvider(),
            _fraConverter,
            env.getValuationTime(),
            bundleResult.getValue().getCurveBuildingBlockBundle(),
            curveLabellers.getValue());
        return Result.success(calculator);
      } else {
        return Result.failure(curveLabellers);
      }
    } else {
      return Result.failure(bundleResult);
    }
  }
  
  @Override
  public Result<FRACalculator> createCalculator(Environment env, ForwardRateAgreementSecurity security) {
    Trade trade = new SimpleTrade(security,
                                  BigDecimal.ONE,
                                  new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                  LocalDate.now(),
                                  OffsetTime.now());
    ForwardRateAgreementTrade tradeWrapper = new ForwardRateAgreementTrade(trade);

    return createCalculator(env, tradeWrapper);
  }

  @Override
  public Result<FRACalculator> createCalculator(Environment env, ForwardRateAgreementTrade trade) {
    Result<MulticurveBundle> bundleResult = _discountingMulticurveCombinerFn.getMulticurveBundle(env, trade);
    ForwardRateAgreementSecurity security = trade.getSecurity();
    Result<HistoricalTimeSeriesBundle> fixings = _htsFn.getFixingsForSecurity(env, security);

    if (Result.allSuccessful(bundleResult, fixings)) {

      Set<String> curveNames = bundleResult.getValue().getCurveBuildingBlockBundle().getData().keySet();
      Result<Map<String, CurveMatrixLabeller>> curveLabellers =
          _curveLabellingFn.getCurveLabellers(curveNames);

      if (curveLabellers.isSuccess()) {

        FRACalculator calculator = new DiscountingFRACalculator(security,
            bundleResult.getValue().getMulticurveProvider(),
            _fraConverter,
            env.getValuationTime(),
            _fixedIncomeConverterDataProvider,
            fixings.getValue(),
            bundleResult.getValue().getCurveBuildingBlockBundle(),
            curveLabellers.getValue());
        return Result.success(calculator);
      } else {
        return Result.failure(curveLabellers);
      }
    } else {
      return Result.failure(bundleResult);
    }
  }

}
