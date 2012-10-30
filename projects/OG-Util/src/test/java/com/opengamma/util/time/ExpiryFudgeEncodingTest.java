/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
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
