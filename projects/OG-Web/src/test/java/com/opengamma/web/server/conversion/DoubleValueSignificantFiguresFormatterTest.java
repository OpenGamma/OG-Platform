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
 * Tests {@link DoubleValueSignificantFiguresFormatter}
 */
@Test(groups = TestGroup.UNIT)
public class DoubleValueSignificantFiguresFormatterTest {

  @Test
  public void test3SF() {
    DoubleValueSignificantFiguresFormatter formatter = new DoubleValueSignificantFiguresFormatter(3, false);
    assertEquals("-1,234", format(formatter, -1234.123));
    assertEquals("-1.23", format(formatter, -1.22678));
    assertEquals("0.0", format(formatter, 0));
    assertEquals("0.00000123", format(formatter, 0.00000123456));
    assertEquals("123", format(formatter, 123.456));
    assertEquals("123,457", format(formatter, 123456.789));
  }
  
  @Test
  public void testLocale() {
    DoubleValueSignificantFiguresFormatter formatter = new DoubleValueSignificantFiguresFormatter(2, false, DecimalFormatSymbols.getInstance(Locale.GERMAN));
    assertEquals("-1.234", format(formatter, -1234.432));
    assertEquals("-12", format(formatter, -12.123));
    assertEquals("0,0", format(formatter, 0));
    assertEquals("0,12", format(formatter, 0.12));
    assertEquals("1.234", format(formatter, 1233.56));
    
    formatter = new DoubleValueSignificantFiguresFormatter(5, false, DecimalFormatSymbols.getInstance(Locale.FRENCH));
    String nbsp = "\u00A0";
    assertEquals("1" + nbsp + "234,5", format(formatter, 1234.4567));
  }
  
  private String format(DoubleValueFormatter formatter, double number) {
    return formatter.format(BigDecimal.valueOf(number));
  }
  
}
