/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ZonedDateTimeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_UTC() {
    ZonedDateTime zdtUTC = ZonedDateTime.of(LocalDateTime.of(2010, 7, 1, 0, 0), ZoneOffset.UTC);
    assertEncodeDecodeCycle(ZonedDateTime.class, zdtUTC);
  }

  public void test_newYork() {
    ZonedDateTime zdtUTC = ZonedDateTime.of(LocalDateTime.of(2010, 7, 1, 0, 0), ZoneOffset.UTC);
    ZonedDateTime zdtPST = ZonedDateTime.ofInstant(zdtUTC.toInstant(), ZoneId.of("America/New_York"));
    assertTrue(zdtUTC.isEqual(zdtPST));
    assertEncodeDecodeCycle(ZonedDateTime.class, zdtPST);
  }

}
