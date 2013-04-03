/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DoubleValueDecimalPlaceFormatter}
 */
@Test(groups = TestGroup.UNIT)
public class DoubleValueDecimalPlaceFormatterTest {

  @Test
  public void testNoDP() {
    DoubleValueDecimalPlaceFormatter formatter = new DoubleValueDecimalPlaceFormatter(0, false);
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
  public void testOneDP() {
    DoubleValueDecimalPlaceFormatter formatter = new DoubleValueDecimalPlaceFormatter(1, false);
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
  public void testFiveDP() {
    DoubleValueDecimalPlaceFormatter formatter = new DoubleValueDecimalPlaceFormatter(5, false);
    assertEquals("0.00000", format(formatter, 0));
    assertEquals("12,345.57874", format(formatter, 12345.57874123));
  }
  
  @Test
  public void testLocale() {
    DoubleValueDecimalPlaceFormatter formatter = new DoubleValueDecimalPlaceFormatter(2, false, DecimalFormatSymbols.getInstance(Locale.GERMAN));
    assertEquals("1.234.567,98", format(formatter, 1234567.98));
    assertEquals("1.234.567,00", format(formatter, 1234567));
    assertEquals("1,23", format(formatter, 1.22666));
    
    formatter = new DoubleValueDecimalPlaceFormatter(2, false, DecimalFormatSymbols.getInstance(Locale.FRENCH));
    String nbsp = "\u00A0";
    assertEquals("1" + nbsp + "234,99", format(formatter, 1234.987654321));
  }
  
  private String format(DoubleValueFormatter formatter, double number) {
    return formatter.format(BigDecimal.valueOf(number));
  }
  
}
