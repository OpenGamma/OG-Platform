/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.threeten.bp.ZonedDateTime;

/**
 * 
 */
public class ScheduleTestUtils {

  public static void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    if (expected == null) {
      assertNull(actual);
      return;
    }
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
