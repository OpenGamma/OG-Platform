/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import static com.opengamma.web.analytics.formatting.ResultsFormatter.CurrencyDisplay.SUPPRESS_CURRENCY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ResultsFormatterTest {

  /**
   * For a specific bug
   */
  @Test
  public void formatHistoryForValueNameWithUnknownType() {
    ResultsFormatter formatter = new ResultsFormatter();
    UniqueId uid = UniqueId.of("scheme", "value");
    ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.POSITION, uid);
    ValueProperties props = ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "fn").get();
    // if this works without an exception then the bug is fixed
    formatter.format(123d, new ValueSpecification("unknown value name", spec, props), TypeFormatter.Format.HISTORY, null);
  }

  @Test
  public void currencyAmountCanHaveCurrencyOutput() {

    ResultsFormatter formatter = new ResultsFormatter();
    CurrencyAmount value = CurrencyAmount.of(Currency.USD, 123.45);
    Object result = formatter.format(value, null, TypeFormatter.Format.CELL, null);

    assertTrue(result instanceof String);
    assertEquals(result, "USD 123.45");
  }

  @Test
  public void currencyAmountCanHaveCurrencySuppressed() {

    ResultsFormatter formatter = new ResultsFormatter(SUPPRESS_CURRENCY);
    CurrencyAmount value = CurrencyAmount.of(Currency.USD, 123.45);
    Object result = formatter.format(value, null, TypeFormatter.Format.CELL, null);

    assertTrue(result instanceof String);
    assertEquals(result, "123.45");
  }

  @Test
  public void unknownValueSpecCanHaveCurrencyOutput() {

    ResultsFormatter formatter = new ResultsFormatter();
    Object result = formatter.format(123.45,
                                     buildValueSpecificationWithCurrency("unknown value name"),
                                     TypeFormatter.Format.CELL,
                                     null);

    assertTrue(result instanceof String);

    // Default is 0 dp as value > 10
    assertEquals(result, "USD 123");
  }

  @Test
  public void unknownValueSpecCanHaveCurrencySuppressed() {

    ResultsFormatter formatter = new ResultsFormatter(SUPPRESS_CURRENCY);
    Object result = formatter.format(123.45,
                                     buildValueSpecificationWithCurrency("unknown value name"),
                                     TypeFormatter.Format.CELL,
                                     null);

    assertTrue(result instanceof String);

    // Default is 0 dp as value > 10
    assertEquals(result, "123");
  }

  @Test
  public void knownValueSpecCanHaveCurrencyOutput() {

    ResultsFormatter formatter = new ResultsFormatter();
    Object result = formatter.format(123.45, buildValueSpecificationWithCurrency(ValueRequirementNames.PRESENT_VALUE), TypeFormatter.Format.CELL, null);

    assertTrue(result instanceof String);

    // Default is 0 dp as value > 10
    assertEquals(result, "USD 123");
  }

  @Test
  public void knownValueSpecCanHaveCurrencySuppressed() {

    ResultsFormatter formatter = new ResultsFormatter(SUPPRESS_CURRENCY);
    Object result = formatter.format(123.45, buildValueSpecificationWithCurrency(ValueRequirementNames.PRESENT_VALUE), TypeFormatter.Format.CELL, null);

    assertTrue(result instanceof String);

    // Default is 0 dp as value > 10
    assertEquals(result, "123");
  }

  private ValueSpecification buildValueSpecificationWithCurrency(String valueName) {

    UniqueId uid = UniqueId.of("scheme", "value");
    ComputationTargetSpecification cts = new ComputationTargetSpecification(ComputationTargetType.POSITION, uid);

    ValueProperties props = ValueProperties.builder()
        .with(ValuePropertyNames.FUNCTION, "fn")
        .with(ValuePropertyNames.CURRENCY, "USD")
        .get();

    return new ValueSpecification(valueName, cts, props);
  }
}
