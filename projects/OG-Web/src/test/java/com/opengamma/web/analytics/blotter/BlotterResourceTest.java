/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.joda.convert.StringConvert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BlotterResourceTest {

  private final StringConvert _stringConvert = BlotterUtils.getStringConvert();
  
  @Test
  public void isSecurity() {
    assertTrue(BlotterResource.isSecurity(ManageableSecurity.class));
    assertTrue(BlotterResource.isSecurity(SwapSecurity.class));
    assertTrue(BlotterResource.isSecurity(GovernmentBondSecurity.class));
    assertFalse(BlotterResource.isSecurity(FixedInterestRateLeg.class));
  }

  @Test
  public void convertBarrierTypes() {
    assertEquals("Up", _stringConvert.convertToString(BarrierType.UP));
    assertEquals("Down", _stringConvert.convertToString(BarrierType.DOWN));
    assertEquals("Double", _stringConvert.convertToString(BarrierType.DOUBLE));
    assertEquals(BarrierType.UP, _stringConvert.convertFromString(BarrierType.class, "Up"));
    assertEquals(BarrierType.DOWN, _stringConvert.convertFromString(BarrierType.class, "Down"));
    assertEquals(BarrierType.DOUBLE, _stringConvert.convertFromString(BarrierType.class, "Double"));
  }

  @Test
  public void convertBarrierDirections() {
    assertEquals("Knock In", _stringConvert.convertToString(BarrierDirection.KNOCK_IN));
    assertEquals("Knock Out", _stringConvert.convertToString(BarrierDirection.KNOCK_OUT));
    assertEquals(BarrierDirection.KNOCK_IN, _stringConvert.convertFromString(BarrierDirection.class, "Knock In"));
    assertEquals(BarrierDirection.KNOCK_OUT, _stringConvert.convertFromString(BarrierDirection.class, "Knock Out"));
  }

  @Test
  public void convertSamplingFrequencies() {
    assertEquals("Daily Close", _stringConvert.convertToString(SamplingFrequency.DAILY_CLOSE));
    assertEquals("Friday", _stringConvert.convertToString(SamplingFrequency.FRIDAY));
    assertEquals("Weekly Close", _stringConvert.convertToString(SamplingFrequency.WEEKLY_CLOSE));
    assertEquals("Continuous", _stringConvert.convertToString(SamplingFrequency.CONTINUOUS));
    assertEquals("One Look", _stringConvert.convertToString(SamplingFrequency.ONE_LOOK));
    assertEquals(SamplingFrequency.DAILY_CLOSE, _stringConvert.convertFromString(SamplingFrequency.class, "Daily Close"));
    assertEquals(SamplingFrequency.FRIDAY, _stringConvert.convertFromString(SamplingFrequency.class, "Friday"));
    assertEquals(SamplingFrequency.WEEKLY_CLOSE, _stringConvert.convertFromString(SamplingFrequency.class, "Weekly Close"));
    assertEquals(SamplingFrequency.CONTINUOUS, _stringConvert.convertFromString(SamplingFrequency.class, "Continuous"));
    assertEquals(SamplingFrequency.ONE_LOOK, _stringConvert.convertFromString(SamplingFrequency.class, "One Look"));
  }

  @Test
  public void convertZonedDateTime() {
    ZonedDateTime date = LocalDate.of(2012, 12, 21).atTime(11, 0).atZone(ZoneId.of("UTC"));
    assertEquals("2012-12-21", _stringConvert.convertToString(date));
    assertEquals(date, _stringConvert.convertFromString(ZonedDateTime.class, "2012-12-21"));
  }
}
