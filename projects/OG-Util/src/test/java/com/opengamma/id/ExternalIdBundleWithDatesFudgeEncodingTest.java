/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.threeten.bp.Month.DECEMBER;
import static org.threeten.bp.Month.JANUARY;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdBundleWithDatesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final LocalDate VALID_FROM = LocalDate.of(2010, JANUARY, 1);
  private static final LocalDate VALID_TO = LocalDate.of(2010, DECEMBER, 1);

  public void test_noDates() {
    ExternalIdWithDates basic1 = ExternalIdWithDates.of(ExternalId.of("A", "B"), null, null);
    ExternalIdWithDates basic2 = ExternalIdWithDates.of(ExternalId.of("C", "D"), null, null);
    ExternalIdBundleWithDates object = ExternalIdBundleWithDates.of(basic1, basic2);
    assertEncodeDecodeCycle(ExternalIdBundleWithDates.class, object);
  }

  public void test_withDates() {
    ExternalIdWithDates basic1 = ExternalIdWithDates.of(ExternalId.of("A", "B"), VALID_FROM, VALID_TO);
    ExternalIdWithDates basic2 = ExternalIdWithDates.of(ExternalId.of("C", "D"), VALID_FROM, VALID_TO);
    ExternalIdBundleWithDates object = ExternalIdBundleWithDates.of(basic1, basic2);
    assertEncodeDecodeCycle(ExternalIdBundleWithDates.class, object);
  }

}
