/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class FlexiDateTimeFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_LD() {
    FlexiDateTime ld = FlexiDateTime.of(LocalDate.of(2010, 7, 1));
    assertEncodeDecodeCycle(FlexiDateTime.class, ld);
  }

  public void test_LDT() {
    FlexiDateTime ldt = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0));
    assertEncodeDecodeCycle(FlexiDateTime.class, ldt);
  }

  public void test_ODT() {
    FlexiDateTime odt = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0).atOffset(ZoneOffset.ofHours(3)));
    assertEncodeDecodeCycle(FlexiDateTime.class, odt);
  }

  public void test_ZDT_UTC() {
    FlexiDateTime zdtUTC = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0, 0).atZone(ZoneOffset.UTC));
    assertEncodeDecodeCycle(FlexiDateTime.class, zdtUTC);
  }

  public void test_ZDT_newYork() {
    FlexiDateTime zdtPST = FlexiDateTime.of(LocalDateTime.of(2010, 7, 1, 13, 0, 0, 0).atZone(ZoneId.of("America/New_York")));
    assertEncodeDecodeCycle(FlexiDateTime.class, zdtPST);
  }

}
