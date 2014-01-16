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
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SpotRateScalingTest {

  private static final double DELTA = 0.00000001;
  private static final SpotRateScaling SCALING = new SpotRateScaling(2d, ImmutableSet.of(CurrencyPair.parse("EUR/USD"),
                                                                                         CurrencyPair.parse("CHF/JPY")));

  private static ValueSpecification valueSpec(String currencyPairStr) {
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    CurrencyPair currencyPair = CurrencyPair.parse(currencyPairStr);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(CurrencyPair.TYPE, currencyPair.getUniqueId());
    return new ValueSpecification("SpotRate", targetSpec, properties);
  }

  @Test
  public void normalPair() {
    assertEquals(8d, SCALING.execute(4d, valueSpec("EUR/USD")), DELTA);
    assertEquals(10d, SCALING.execute(5d, valueSpec("CHF/JPY")), DELTA);
  }

  @Test
  public void inversePair() {
    assertEquals(2d, SCALING.execute(4d, valueSpec("USD/EUR")), DELTA);
    assertEquals(3d, SCALING.execute(6d, valueSpec("JPY/CHF")), DELTA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedTargetType() {
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.CURRENCY,
                                                                                   Currency.GBP.getUniqueId());
    ValueSpecification valueSpec = new ValueSpecification("SpotRate", targetSpec, properties);
    SCALING.execute(2d, valueSpec);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedCurrencyPair() {
    SCALING.execute(2d, valueSpec("GBP/USD"));
  }
}
