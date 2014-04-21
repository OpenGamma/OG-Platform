/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.commodity.calculator.CommodityFutureOptionBlackForwardGammaCalculator;
import com.opengamma.analytics.financial.commodity.definition.AgricultureFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.EnergyFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Checks the wiring of the  CommodityFutureOptionPresentValueCalculator
 */
@Test(groups = TestGroup.UNIT)
public class CommodityFutureOptionForwardGammaCalculatorTest extends CommodityFutureOptionTestDefaults {

  private static final CommodityFutureOptionBlackForwardGammaCalculator PRICER = CommodityFutureOptionBlackForwardGammaCalculator.getInstance();

  public void testAgricultureFutureOption() {
    final double answer = 0.0950818333883517;

    final AgricultureFutureDefinition definition = new AgricultureFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, UNIT_AMOUNT, null, null, AMOUNT, "tonnes",
        SettlementType.CASH, 0, Currency.GBP, SETTLEMENT_DATE);
    final AgricultureFuture future = definition.toDerivative(A_DATE);
    final AgricultureFutureOption option = new AgricultureFutureOption(EXPIRY, future, STRIKE, EXERCISE, true);
    final double pv = option.accept(PRICER, MARKET);
    assertEquals(answer, pv * option.getUnderlying().getUnitAmount(), TOLERANCE);
  }

  public void testEnergyFutureOption() {
    final double answer = 0.09508183338835174;

    final EnergyFutureDefinition definition = new EnergyFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, UNIT_AMOUNT, null, null, AMOUNT, "tonnes", SettlementType.CASH, 0,
        Currency.GBP, SETTLEMENT_DATE);
    final EnergyFuture future = definition.toDerivative(A_DATE);
    final EnergyFutureOption option = new EnergyFutureOption(EXPIRY, future, STRIKE, EXERCISE, true);
    final double pv = option.accept(PRICER, MARKET);
    assertEquals(answer, pv * option.getUnderlying().getUnitAmount(), TOLERANCE);
  }

  public void testMetalFutureOption() {
    final double answer = 0.0950818333883517;

    final MetalFutureDefinition definition = new MetalFutureDefinition(EXPIRY_DATE, AN_UNDERLYING, UNIT_AMOUNT, null, null, AMOUNT, "tonnes", SettlementType.CASH, 0,
        Currency.GBP, SETTLEMENT_DATE);
    final MetalFuture future = definition.toDerivative(A_DATE);
    final MetalFutureOption option = new MetalFutureOption(EXPIRY, future, STRIKE, EXERCISE, true);
    final double pv = option.accept(PRICER, MARKET);
    assertEquals(answer, pv * option.getUnderlying().getUnitAmount(), TOLERANCE);
  }
}
