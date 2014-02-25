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
import com.opengamma.engine.function.FunctionExecutionContext;
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

  /* Scaling of 0.1 means +10%, i.e. multiply by 1.1 */
  private static final SpotRateScaling UP_10 = new SpotRateScaling(1.1d, ImmutableSet.of(CurrencyPair.parse("EUR/USD"),
                                                                                         CurrencyPair.parse("CHF/JPY")));

  /* Scaling of -0.2 means -20%, i.e. multiply by 0.8 */
  private static final SpotRateScaling DOWN_20 = new SpotRateScaling(0.8d, ImmutableSet.of(CurrencyPair.parse("EUR/USD"),
                                                                                           CurrencyPair.parse("CHF/JPY")));

  private static ValueSpecification valueSpec(String currencyPairStr) {
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    CurrencyPair currencyPair = CurrencyPair.parse(currencyPairStr);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(CurrencyPair.TYPE, currencyPair.getUniqueId());
    return new ValueSpecification("SpotRate", targetSpec, properties);
  }

  @Test
  public void normalPair() {
    assertEquals(4.4, UP_10.execute(4.0, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(5.5, UP_10.execute(5.0, valueSpec("CHF/JPY"), new FunctionExecutionContext()), DELTA);
    assertEquals(3.2, DOWN_20.execute(4.0, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(4.0, DOWN_20.execute(5.0, valueSpec("CHF/JPY"), new FunctionExecutionContext()), DELTA);
  }

  @Test
  public void inversePair() {
    assertEquals(0.22727272727272727, UP_10.execute(0.25, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.18181818181818182, UP_10.execute(0.2, valueSpec("JPY/CHF"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.3125, DOWN_20.execute(0.25, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.25, DOWN_20.execute(0.2, valueSpec("JPY/CHF"), new FunctionExecutionContext()), DELTA);
  }

  @Test
  public void boundedRate() {
    SpotRateScaling up = new SpotRateScaling(2d, 0, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(4d, up.execute(2d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(6d, up.execute(5d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);

    SpotRateScaling down = new SpotRateScaling(0.5, 0.3, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(1d, down.execute(2d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.3, down.execute(0.5, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
  }

  @Test
  public void boundedInverseRate() {
    SpotRateScaling up = new SpotRateScaling(2d, 0, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(1d, up.execute(2d, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.166666666, up.execute(0.2, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);

    SpotRateScaling down = new SpotRateScaling(0.5, 0.2, 6d, CurrencyPair.parse("EUR/USD"));
    assertEquals(0.5, down.execute(0.25, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(5, down.execute(4d, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedTargetType() {
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.CURRENCY,
                                                                                   Currency.GBP.getUniqueId());
    ValueSpecification valueSpec = new ValueSpecification("SpotRate", targetSpec, properties);
    UP_10.execute(2d, valueSpec, new FunctionExecutionContext());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedCurrencyPair() {
    UP_10.execute(2d, valueSpec("GBP/USD"), new FunctionExecutionContext());
  }
}
