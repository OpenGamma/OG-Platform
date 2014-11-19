/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bond;

import java.util.Map;

import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.financial.analytics.conversion.BondAndBondFutureTradeConverter;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.sesame.CurveDefinitionFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.IssuerProviderBundle;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.trade.BondTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

/**
 * Implementation of the BondCalculatorFactory that uses the discounting calculator to return values.
 */
public class DiscountingBondCalculatorFactory implements BondCalculatorFactory {

  private final BondAndBondFutureTradeConverter _converter;
  private final IssuerProviderFn _issuerProviderFn;
  private final CurveDefinitionFn _curveDefinitionFn;
  private final MarketDataFn _marketDataFn;

  /**
   * Creates the factory.
   *
   * @param converter converter for transforming a bond
   * @param issuerProviderFn multicurve bundle for curves by issuer.
   * @param curveDefinitionFn the curve definition function, not null.
   * @param marketDataFn the MarketData function, not null.
   *
   */
  public DiscountingBondCalculatorFactory(BondAndBondFutureTradeConverter converter,
                                          IssuerProviderFn issuerProviderFn,
                                          CurveDefinitionFn curveDefinitionFn,
                                          MarketDataFn marketDataFn) {
    _marketDataFn = ArgumentChecker.notNull(marketDataFn, "marketDataFn");
    _converter = ArgumentChecker.notNull(converter, "converter");
    _issuerProviderFn = ArgumentChecker.notNull(issuerProviderFn, "issuerProviderFn");
    _curveDefinitionFn = ArgumentChecker.notNull(curveDefinitionFn, "curveDefinitionFn");
  }

  @Override
  public Result<BondCalculator> createCalculator(Environment env, BondTrade tradeWrapper) {

    Result<IssuerProviderBundle> bundleResult = _issuerProviderFn.getMulticurveBundle(env, tradeWrapper.getTrade());

    if (bundleResult.isSuccess()) {
      CurveBuildingBlockBundle blocks = bundleResult.getValue().getCurveBuildingBlockBundle();

      Result<Map<String, CurveDefinition>> curveDefinitions =
          _curveDefinitionFn.getCurveDefinitions(blocks.getData().keySet());

      if (!curveDefinitions.isSuccess()) {
        return Result.failure(curveDefinitions);
      }

      ParameterIssuerProviderInterface curves = bundleResult.getValue().getParameterIssuerProvider();

      BondCalculator calculator = new DiscountingBondCalculator(tradeWrapper, curves, blocks,  _converter, env,
                                                                curveDefinitions.getValue(), _marketDataFn);
      return Result.success(calculator);
    } else {
      return Result.failure(bundleResult);
    }


  }
}
