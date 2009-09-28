/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

/**
 * 
 * 
 * @author emcleod
 */
public class DateUtilTest {

  @Test
  public void testIsLeapYear() {
    ZonedDateTime nonLeapYear = DateUtil.getUTCDate(2001, 1, 1);
    assertFalse(DateUtil.isLeapYear(nonLeapYear));
    nonLeapYear = DateUtil.getUTCDate(1900, 1, 1);
    assertFalse(DateUtil.isLeapYear(nonLeapYear));
    ZonedDateTime leapYear = DateUtil.getUTCDate(2004, 1, 1);
    assertTrue(DateUtil.isLeapYear(leapYear));
    leapYear = DateUtil.getUTCDate(2000, 1, 1);
    assertTrue(DateUtil.isLeapYear(leapYear));
  }
}
