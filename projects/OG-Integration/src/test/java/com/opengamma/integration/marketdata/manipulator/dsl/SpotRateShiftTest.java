/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SpotRateShiftTest {

  private static final double DELTA = 0.00000001;
  private static final SpotRateShift SHIFT = new SpotRateShift(1d, ImmutableSet.of(CurrencyPair.parse("EUR/USD"),
                                                                                   CurrencyPair.parse("CHF/JPY")));

  private static ValueSpecification valueSpec(String currencyPairStr) {
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    CurrencyPair currencyPair = CurrencyPair.parse(currencyPairStr);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(CurrencyPair.TYPE, currencyPair.getUniqueId());
    return new ValueSpecification("SpotRate", targetSpec, properties);
  }

  @Test
  public void normalPair() {
    assertEquals(3d, SHIFT.execute(2d, valueSpec("EUR/USD")), DELTA);
    assertEquals(5d, SHIFT.execute(4d, valueSpec("CHF/JPY")), DELTA);
  }

  @Test
  public void inversePair() {
    assertEquals(0.6666666666d, SHIFT.execute(2d, valueSpec("USD/EUR")), DELTA);
    assertEquals(0.8d, SHIFT.execute(4d, valueSpec("JPY/CHF")), DELTA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedTargetType() {
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.CURRENCY,
                                                                                   Currency.GBP.getUniqueId());
    ValueSpecification valueSpec = new ValueSpecification("SpotRate", targetSpec, properties);
    SHIFT.execute(2d, valueSpec);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedCurrencyPair() {
    SHIFT.execute(2d, valueSpec("GBP/USD"));
  }
}
