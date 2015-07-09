/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.fxforward;

import java.math.BigDecimal;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.trade.FXForwardTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Result;

public class FXForwardDiscountingCalculatorFn implements FXForwardCalculatorFn {

  /**
   * Factory for creating a calculator for FX Forward securities.
   */
  private final FXForwardCalculatorFactory _factory;

  /**
   * Generates a combined multicurve bundle suitable for use with a particular security.
   */
  private final DiscountingMulticurveCombinerFn _discountingMulticurveCombinerFn;

  public FXForwardDiscountingCalculatorFn(FXForwardCalculatorFactory factory,
                                          DiscountingMulticurveCombinerFn discountingMulticurveCombinerFn) {
    _factory = ArgumentChecker.notNull(factory, "factory");
    _discountingMulticurveCombinerFn =
        ArgumentChecker.notNull(discountingMulticurveCombinerFn, "discountingMulticurveCombinerFn");
  }

  @Override
  public Result<FXForwardCalculator> generateCalculator(Environment env, final FXForwardSecurity security) {
    Result<MulticurveBundle> bundleResult = createBundle(env, security);

    if (bundleResult.isSuccess()) {
      MulticurveBundle value = bundleResult.getValue();
      return Result.success(_factory.createCalculator(security,
                                                      value.getMulticurveProvider(),
                                                      value.getCurveBuildingBlockBundle()));
    } else {
      return Result.failure(bundleResult);
    }
  }

  private Result<MulticurveBundle> createBundle(Environment env, FXForwardSecurity security) {
    Trade trade = new SimpleTrade(security,
                                  BigDecimal.ONE,
                                  new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                  LocalDate.now(),
                                  OffsetTime.now());
    FXForwardTrade tradeWrapper = new FXForwardTrade(trade);
    return _discountingMulticurveCombinerFn.getMulticurveBundle(env, tradeWrapper);
  }
}
