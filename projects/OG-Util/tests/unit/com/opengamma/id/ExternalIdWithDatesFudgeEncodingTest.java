/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class ExternalIdWithDatesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final LocalDate VALID_FROM = LocalDate.of(2010, MonthOfYear.JANUARY, 1);
  private static final LocalDate VALID_TO = LocalDate.of(2010, MonthOfYear.DECEMBER, 1);

  public void test_noDates() {
    ExternalIdWithDates object = ExternalIdWithDates.of(ExternalId.of("A", "B"), null, null);
    assertEncodeDecodeCycle(ExternalIdWithDates.class, object);
  }

  public void test_withDates() {
    ExternalIdWithDates object = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);
    assertEncodeDecodeCycle(ExternalIdWithDates.class, object);
  }

}
