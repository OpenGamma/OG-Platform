/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DoubleValueSizeBasedDecimalPlaceFormatter}.
 */
@Test(groups = TestGroup.UNIT)
public class DoubleValueSizeBasedDecimalPlaceFormatterTest {

  @Test
  public void test_alwaysNoDP() {
    DoubleValueSizeBasedDecimalPlaceFormatter formatter = new DoubleValueSizeBasedDecimalPlaceFormatter(0, 0, 10, true);
    assertEquals("-1,234", format(formatter, -1234.123));
    assertEquals("-10", format(formatter, -9.98543));
    assertEquals("0", format(formatter, -0.1));
    assertEquals("0", format(formatter, 0));
    assertEquals("0", format(formatter, 0.1));
    assertEquals("123", format(formatter, 123.456));
    assertEquals("124", format(formatter, 123.556));
    assertEquals("1,234", format(formatter, 1234.123));
    assertEquals("12,345", format(formatter, 12345.123));
    assertEquals("123,456", format(formatter, 123456.123));
    assertEquals("1,234,567", format(formatter, 1234567.123));
    assertEquals("12,345,678", format(formatter, 12345678.123));
  }

  @Test
  public void test_alwaysOneDP() {
    DoubleValueSizeBasedDecimalPlaceFormatter formatter = new DoubleValueSizeBasedDecimalPlaceFormatter(1, 1, 10, true);
    assertEquals("-12,345.6", format(formatter, -12345.57874));
    assertEquals("-60.0", format(formatter, -59.97));
    assertEquals("-0.2", format(formatter, -0.15123));
    assertEquals("-0.1", format(formatter, -0.14967));
    assertEquals("0.0", format(formatter, 0));
    assertEquals("0.1", format(formatter, 0.123));
    assertEquals("0.2", format(formatter, 0.159));
    assertEquals("123.4", format(formatter, 123.446));
    assertEquals("123.5", format(formatter, 123.456));
    assertEquals("12,345.6", format(formatter, 12345.57874));
  }

  @Test
  public void test_adaptive() {
    DoubleValueSizeBasedDecimalPlaceFormatter formatter = new DoubleValueSizeBasedDecimalPlaceFormatter(2, 0, 9, true);
    assertEquals("-12,346", format(formatter, -12345.57874));
    assertEquals("-12,345", format(formatter, -12345.49999));
    assertEquals("-60", format(formatter, -59.97));
    assertEquals("-10", format(formatter, -10.01));
    assertEquals("-9.97", format(formatter, -9.97));
    assertEquals("-0.15", format(formatter, -0.15123));
    assertEquals("-0.15", format(formatter, -0.14967));
    assertEquals("-0.15", format(formatter, -0.14500));
    assertEquals("-0.14", format(formatter, -0.14499));
    assertEquals("0.00", format(formatter, 0));
    assertEquals("0.12", format(formatter, 0.123));
    assertEquals("0.16", format(formatter, 0.159));
    assertEquals("9.98", format(formatter, 9.98));
    assertEquals("10", format(formatter, 10.01));
    assertEquals("123", format(formatter, 123.446));
    assertEquals("124", format(formatter, 123.500));
    assertEquals("12,346", format(formatter, 12345.57874));
  }

  private String format(DoubleValueFormatter formatter, double number) {
    return formatter.format(BigDecimal.valueOf(number));
  }

}
