/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ExpiryFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void test_accuracyHour() {
    Expiry object = new Expiry(ZonedDateTime.now(), ExpiryAccuracy.HOUR_DAY_MONTH_YEAR);
    assertEncodeDecodeCycle(Expiry.class, object);
  }

  public void test_accuracyDay() {
    Expiry object = new Expiry(ZonedDateTime.now(), ExpiryAccuracy.DAY_MONTH_YEAR);
    assertEncodeDecodeCycle(Expiry.class, object);
  }

  public void test_accuracyYear() {
    Expiry object = new Expiry(ZonedDateTime.now(), ExpiryAccuracy.YEAR);
    assertEncodeDecodeCycle(Expiry.class, object);
  }

}
