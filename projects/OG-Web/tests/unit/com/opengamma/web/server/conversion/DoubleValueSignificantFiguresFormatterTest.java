/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import static org.testng.AssertJUnit.assertEquals;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.testng.annotations.Test;

/**
 * Tests {@link DoubleValueSignificantFiguresFormatter}
 */
public class DoubleValueSignificantFiguresFormatterTest {

  @Test
  public void test3SF() {
    DoubleValueSignificantFiguresFormatter formatter = new DoubleValueSignificantFiguresFormatter(3, false);
    assertEquals("-1,234", formatter.format(-1234.123));
    assertEquals("-1.23", formatter.format(-1.22678));
    assertEquals("0.0", formatter.format(0));
    assertEquals("0.00000123", formatter.format(0.00000123456));
    assertEquals("123", formatter.format(123.456));
    assertEquals("123,457", formatter.format(123456.789));
  }
  
  @Test
  public void testLocale() {
    DoubleValueSignificantFiguresFormatter formatter = new DoubleValueSignificantFiguresFormatter(2, false, DecimalFormatSymbols.getInstance(Locale.GERMAN));
    assertEquals("-1.234", formatter.format(-1234.432));
    assertEquals("-12", formatter.format(-12.123));
    assertEquals("0,0", formatter.format(0));
    assertEquals("0,12", formatter.format(0.12));
    assertEquals("1.234", formatter.format(1233.56));
    
    formatter = new DoubleValueSignificantFiguresFormatter(5, false, DecimalFormatSymbols.getInstance(Locale.FRENCH));
    String nbsp = "\u00A0";
    assertEquals("1" + nbsp + "234,5", formatter.format(1234.4567));
  }
  
}
