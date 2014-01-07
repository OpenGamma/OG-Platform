/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class SwapSecurityFudgeBuilderTest extends AbstractFudgeBuilderTestCase {
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2013, 7, 1);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2013, 7, 1);
  private static final ZonedDateTime MATURITY = DateUtils.getUTCDate(2023, 7, 1);
  private static final String COUNTERPARTY = "OG";
  private static final DayCount DC = DayCounts.ACT_360;
  private static final Frequency FREQUENCY = SimpleFrequency.SEMI_ANNUAL;
  private static final ExternalId REGION_ID = ExternalId.of("Test", "US");
  private static final BusinessDayConvention BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Notional NOTIONAL = new InterestRateNotional(Currency.USD, 10000);
  private static final boolean EOM = true;

  @Test
  public void testSwapSecurity() {
    final ExternalId referenceId = ExternalId.of("Test", "ASD");
    final SwapLeg payLeg = new FixedInterestRateLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, 0.05);
    final SwapLeg receiveLeg = new FloatingInterestRateLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, referenceId, FloatingRateType.OIS);
    final SwapSecurity security = new SwapSecurity(TRADE_DATE, EFFECTIVE_DATE, MATURITY, COUNTERPARTY, payLeg, receiveLeg);
    assertEncodeDecodeCycle(SwapSecurity.class, security);
  }

  @Test
  public void testZeroCouponInflationSwapSecurity() {
    final FixedInflationSwapLeg payLeg = new FixedInflationSwapLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, 0.002);
    final InflationIndexSwapLeg receiveLeg = new InflationIndexSwapLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, ExternalId.of("Test", "SDF"),
        2, 3, InterpolationMethod.MONTH_START_LINEAR);
    final ZeroCouponInflationSwapSecurity security = new ZeroCouponInflationSwapSecurity(TRADE_DATE, EFFECTIVE_DATE, MATURITY, COUNTERPARTY, payLeg, receiveLeg);
    assertEncodeDecodeCycle(ZeroCouponInflationSwapSecurity.class, security);
  }

  @Test
  public void testYearOnYearInflationSwapSecurity() {
    final FixedInflationSwapLeg payLeg = new FixedInflationSwapLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, 0.002);
    final InflationIndexSwapLeg receiveLeg = new InflationIndexSwapLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, ExternalId.of("Test", "SDF"),
        2, 3, InterpolationMethod.MONTH_START_LINEAR);
    final YearOnYearInflationSwapSecurity security = new YearOnYearInflationSwapSecurity(TRADE_DATE, EFFECTIVE_DATE, MATURITY, COUNTERPARTY, payLeg, receiveLeg,
        true, true, Tenor.TEN_YEARS);
    assertEncodeDecodeCycle(YearOnYearInflationSwapSecurity.class, security);
  }

}
