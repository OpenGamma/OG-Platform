/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleFrequencyFactoryTest {

  private final SimpleFrequencyFactory _factory = SimpleFrequencyFactory.INSTANCE;

  @Test
  public void getFrequencyByPeriodCount() {
    assertEquals(SimpleFrequency.DAILY, _factory.getFrequency(365));
    assertEquals(SimpleFrequency.WEEKLY, _factory.getFrequency(52));
    assertEquals(SimpleFrequency.BIWEEKLY, _factory.getFrequency(26));
    assertEquals(SimpleFrequency.TWENTY_EIGHT_DAYS, _factory.getFrequency(13));
    assertEquals(SimpleFrequency.MONTHLY, _factory.getFrequency(12));
    assertEquals(SimpleFrequency.BIMONTHLY, _factory.getFrequency(6));
    assertEquals(SimpleFrequency.QUARTERLY, _factory.getFrequency(4));
    assertEquals(SimpleFrequency.SEMI_ANNUAL, _factory.getFrequency(2));
    assertEquals(SimpleFrequency.ANNUAL, _factory.getFrequency(1));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void enumerateAvailableFrequencies() {
    List<SimpleFrequency> frequencies = Lists.newArrayList(_factory.enumerateAvailableFrequencies());
    assertEquals(frequencies, Lists.newArrayList(
        SimpleFrequency.CONTINUOUS,
        SimpleFrequency.DAILY,
        SimpleFrequency.WEEKLY,
        SimpleFrequency.BIWEEKLY,
        SimpleFrequency.TWENTY_EIGHT_DAYS,
        SimpleFrequency.MONTHLY,
        SimpleFrequency.BIMONTHLY,
        SimpleFrequency.QUARTERLY,
        SimpleFrequency.FOUR_MONTHS,
        SimpleFrequency.FIVE_MONTHS,
        SimpleFrequency.SEMI_ANNUAL,
        SimpleFrequency.SEVEN_MONTHS,
        SimpleFrequency.EIGHT_MONTHS,
        SimpleFrequency.NINE_MONTHS,
        SimpleFrequency.TEN_MONTHS,
        SimpleFrequency.ELEVEN_MONTHS,
        SimpleFrequency.ANNUAL,
        SimpleFrequency.NEVER));
  }
}
