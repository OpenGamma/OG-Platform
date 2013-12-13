/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class SwapLegFudgeBuilderTest extends AbstractFudgeBuilderTestCase {
  private static final DayCount DC = DayCounts.ACT_360;
  private static final Frequency FREQUENCY = SimpleFrequency.SEMI_ANNUAL;
  private static final ExternalId REGION_ID = ExternalId.of("Test", "US");
  private static final BusinessDayConvention BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Notional NOTIONAL = new InterestRateNotional(Currency.USD, 10000);
  private static final boolean EOM = true;
  private static final ExternalId REF_ID = ExternalId.of("Test", "ASD");

  @Test
  public void testFixedInterestRateLeg() {
    final FixedInterestRateLeg leg = new FixedInterestRateLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, 0.05);
    assertEncodeDecodeCycle(FixedInterestRateLeg.class, leg);
  }

  @Test
  public void testFloatingInterestRateLeg() {
    final FloatingInterestRateLeg leg = new FloatingInterestRateLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, REF_ID, FloatingRateType.OIS);
    assertEncodeDecodeCycle(FloatingInterestRateLeg.class, leg);
  }

  @Test
  public void testFloatingSpreadIRLeg() {
    final FloatingSpreadIRLeg leg = new FloatingSpreadIRLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, REF_ID, FloatingRateType.CMS, 0.002);
    assertEncodeDecodeCycle(FloatingSpreadIRLeg.class, leg);
  }

  @Test
  public void testFloatingGearingIRLeg() {
    final FloatingGearingIRLeg leg = new FloatingGearingIRLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, REF_ID, FloatingRateType.IBOR, 2);
    assertEncodeDecodeCycle(FloatingGearingIRLeg.class, leg);
  }

  @Test
  public void testFixedInflationSwapLeg() {
    final FixedInflationSwapLeg leg = new FixedInflationSwapLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, 0.002);
    assertEncodeDecodeCycle(FixedInflationSwapLeg.class, leg);
  }

  @Test
  public void testInflationIndexSwapLeg() {
    final InflationIndexSwapLeg leg = new InflationIndexSwapLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, ExternalId.of("Test", "SDF"),
        2, 3, InterpolationMethod.MONTH_START_LINEAR);
    assertEncodeDecodeCycle(InflationIndexSwapLeg.class, leg);
  }
}
