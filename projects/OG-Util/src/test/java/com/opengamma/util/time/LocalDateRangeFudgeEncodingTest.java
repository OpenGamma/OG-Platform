/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class LocalDateRangeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_inclusive() {
    LocalDateRange range = LocalDateRange.of(LocalDate.of(2010, 7, 1), LocalDate.of(2010, 8, 1), true);
    assertEncodeDecodeCycle(LocalDateRange.class, range);
  }

  public void test_exclusive() {
    LocalDateRange range = LocalDateRange.of(LocalDate.of(2010, 7, 1), LocalDate.of(2010, 8, 1), false);
    assertEncodeDecodeCycle(LocalDateRange.class, range);
  }

}
