/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bond;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCleanPriceCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondTransactionDiscountingMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.ZSpreadIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeConverter;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.sesame.CurveMatrixLabeller;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.marketdata.FieldName;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.trade.BondTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Discounting calculator for bond.
 */
public class DiscountingBondCalculator implements BondCalculator {

  /** Calculator for PV from curves */
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();

  /** Calculators for PV, yield and price from yield, clean price or yield */
  private static final BondTransactionDiscountingMethod BTDM = BondTransactionDiscountingMethod.getInstance();
  private static final BondSecurityDiscountingMethod BSDM = BondSecurityDiscountingMethod.getInstance();

  private static final PV01CurveParametersCalculator<ParameterIssuerProviderInterface> PV01C =
      new PV01CurveParametersCalculator<>(PresentValueCurveSensitivityIssuerCalculator.getInstance());

  private static final PresentValueCurveSensitivityIssuerCalculator PVCSDC =
      PresentValueCurveSensitivityIssuerCalculator.getInstance();

  private static final ParameterSensitivityParameterCalculator<ParameterIssuerProviderInterface> PSC =
      new ParameterSensitivityParameterCalculator<>(PVCSDC);

  private static final MarketQuoteSensitivityBlockCalculator<ParameterIssuerProviderInterface> MQSBC =
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final ZSpreadIssuerCalculator ZSC = ZSpreadIssuerCalculator.getInstance();

  private static final YieldFromCleanPriceCalculator YFCPC = YieldFromCleanPriceCalculator.getInstance();

  private static final double BASIS_POINT_FACTOR = 1.0E-4;

  private final BondFixedTransaction _derivative;

  private final ParameterIssuerProviderInterface _curves;

  private final CurveBuildingBlockBundle _blocks;

  private final Map<String, CurveMatrixLabeller> _curveLabellers;

  private final MarketDataFn _marketDataFn;

  private final Environment _env;

  private BondTrade _trade;

  /**
   * Creates a calculator for a InterestRateSwapSecurity.
   *  @param trade the bond trade to calculate values for
   * @param curves the ParameterIssuerProviderInterface
   * @param blocks the CurveBuildingBlockBundle
   * @param converter the BondAndBondFutureTradeConverter
   * @param env the Environment
   * @param curveLabellers the curve labellers
   * @param marketDataFn the Market Data Function
   */
  public DiscountingBondCalculator(BondTrade trade,
                                   ParameterIssuerProviderInterface curves,
                                   CurveBuildingBlockBundle blocks,
                                   BondAndBondFutureTradeConverter converter,
                                   Environment env,
                                   Map<String, CurveMatrixLabeller> curveLabellers,
                                   MarketDataFn marketDataFn) {

    ArgumentChecker.notNull(converter, "converter");

    _trade = ArgumentChecker.notNull(trade, "trade");
    _env = ArgumentChecker.notNull(env, "valuationTime");
    _blocks = ArgumentChecker.notNull(blocks, "blocks");
    _curves = ArgumentChecker.notNull(curves, "curves");
    _marketDataFn = ArgumentChecker.notNull(marketDataFn, "marketDataFn");
    _curveLabellers = ArgumentChecker.notNull(curveLabellers, "curveLabellers");
    _derivative = createInstrumentDerivative(trade, converter, env.getValuationTime());

  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePresentValueFromCurves() {
    return Result.success(_derivative.accept(PVIC, _curves));
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePresentValueFromClean() {
    Result<Double> marketResult = calculateCleanPriceMarket();
    if (marketResult.isSuccess()) {
      return Result.success(BTDM.presentValueFromCleanPrice(_derivative,
                                                            _curves.getIssuerProvider(),
                                                            marketResult.getValue()));
    } else {
      return Result.failure(marketResult);
    }
  }

  @Override
  public Result<MultipleCurrencyAmount> calculatePresentValueFromYield() {
    Result<Double> marketYield = calculateYieldToMaturityMarket();
    if (marketYield.isSuccess()) {
      return Result.success(BTDM.presentValueFromYield(_derivative,
                                                       _curves.getIssuerProvider(),
                                                       marketYield.getValue()));
    } else {
      return Result.failure(marketYield);
    }
  }

  @Override
  public Result<Double> calculateCleanPriceMarket() {
    return _marketDataFn.getMarketValue(_env, _trade.getSecurity().getExternalIdBundle());
  }

  @Override
  public Result<Double> calculateCleanPriceFromCurves() {
    return Result.success(BSDM.cleanPriceFromCurves(_derivative.getBondStandard(), _curves.getIssuerProvider()));
  }

  @Override
  public Result<Double> calculateCleanPriceFromYield() {
    Result<Double> marketYield = calculateYieldToMaturityMarket();
    if (marketYield.isSuccess()) {
      return Result.success(BSDM.cleanPriceFromYield(_derivative.getBondStandard(), marketYield.getValue()));
    } else {
      return Result.failure(marketYield);
    }
  }

  @Override
  public Result<Double> calculateYieldToMaturityFromCleanPrice() {
    Result<Double> marketCleanPrice = calculateCleanPriceMarket();
    if (marketCleanPrice.isSuccess()) {
      return Result.success(BSDM.yieldFromCleanPrice(_derivative.getBondStandard(), 
          marketCleanPrice.getValue()));
    } else {
      return Result.failure(marketCleanPrice);
    }
  }

  @Override
  public Result<Double> calculateYieldToMaturityFromCurves() {
    return Result.success(BSDM.yieldFromCurves(_derivative.getBondStandard(), _curves.getIssuerProvider()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Result<Double> calculateYieldToMaturityMarket() {
    return (Result<Double>) _marketDataFn.getValue(_env, _trade.getSecurity().getExternalIdBundle(),
        FieldName.of(MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID));
  }

  @Override
  public Result<BucketedCurveSensitivities> calculateBucketedPV01() {
    MultipleCurrencyParameterSensitivity sensitivity = MQSBC
        .fromInstrument(_derivative, _curves, _blocks)
        .multipliedBy(BASIS_POINT_FACTOR);
    Map<Pair<String, Currency>, DoubleLabelledMatrix1D> labelledMatrix1DMap = new HashMap<>();
    for (Map.Entry<Pair<String, Currency>, DoubleMatrix1D> entry : sensitivity.getSensitivities().entrySet()) {
      CurveMatrixLabeller labeller = _curveLabellers.get(entry.getKey().getFirst());
      DoubleLabelledMatrix1D matrix = labeller.labelMatrix(entry.getValue());
      labelledMatrix1DMap.put(entry.getKey(), matrix);
    }
    return Result.success(BucketedCurveSensitivities.of(labelledMatrix1DMap));
  }

  @Override
  public Result<ReferenceAmount<Pair<String, Currency>>> calculatePV01() {
    return Result.success(_derivative.accept(PV01C, _curves));
  }
  
  @Override
  public Result<Double> calculateZSpread() {
    Result<Double> marketResult = calculateCleanPriceMarket();
    if (marketResult.isSuccess()) {
      ObjectsPair<IssuerProviderInterface, Double> pair = ObjectsPair.of(_curves.getIssuerProvider(),
                                                                         marketResult.getValue());
      return Result.success(_derivative.accept(ZSC, pair));
    } else {
      return Result.failure(marketResult);
    }
  }

  private BondFixedTransaction createInstrumentDerivative(BondTrade bondTrade,
                                                          BondAndBondFutureTradeConverter converter,
                                                          ZonedDateTime valuationTime) {
    InstrumentDefinition<?> definition = converter.convert(bondTrade.getTrade());
    return ((BondFixedTransactionDefinition) definition).toDerivative(valuationTime);
  }

}
