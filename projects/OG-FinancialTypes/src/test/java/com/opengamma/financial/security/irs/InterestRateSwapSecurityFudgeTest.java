/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

import java.util.HashSet;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.DateRelativeTo;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.FixedInterestRateSwapLegConvention;
import com.opengamma.financial.convention.FloatingInterestRateSwapLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test for IRS fudge encoding & decoding.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateSwapSecurityFudgeTest extends AbstractFudgeBuilderTestCase {

  private static HashSet<ExternalId> GBLO = Sets.newHashSet(ExternalSchemes.isdaHoliday("GBLO"));
  private static HashSet<ExternalId> USNYGBLO = Sets.newHashSet(ExternalSchemes.isdaHoliday("USNY,GBLO"));

  private static FixedInterestRateSwapLegConvention USD_FIXED_3M_EOM_CONVENTION;
  private static FixedInterestRateSwapLeg USD_FIX_LEG;
  private static FloatingInterestRateSwapLegConvention USD_LIBOR_3M_EOM_CONVENTION;
  private static FloatingInterestRateSwapLeg USD_FLOAT_LEG;

  private static final BusinessDayConvention MF = BusinessDayConventions.MODIFIED_FOLLOWING;

  static {
    USD_FIXED_3M_EOM_CONVENTION = new FixedInterestRateSwapLegConvention("Test1", ExternalIdBundle.of("Scheme", "TEST FIXED"));
    USD_FIXED_3M_EOM_CONVENTION.setDayCountConvention(DayCounts.ACT_360);
    USD_FIXED_3M_EOM_CONVENTION.setCalculationCalendars(USNYGBLO);
    USD_FIXED_3M_EOM_CONVENTION.setMaturityCalendars(USNYGBLO);
    USD_FIXED_3M_EOM_CONVENTION.setPaymentCalendars(USNYGBLO);
    USD_FIXED_3M_EOM_CONVENTION.setPaymentFrequency(SimpleFrequency.QUARTERLY);
    USD_FIXED_3M_EOM_CONVENTION.setPaymentRelativeTo(DateRelativeTo.END);
    USD_FIXED_3M_EOM_CONVENTION.setSettlementDays(2);
    USD_FIXED_3M_EOM_CONVENTION.setPaymentDayConvention(MF);
    USD_FIXED_3M_EOM_CONVENTION.setCalculationBusinessDayConvention(MF);
    USD_FIXED_3M_EOM_CONVENTION.setCalculationFrequency(SimpleFrequency.QUARTERLY);
    USD_FIXED_3M_EOM_CONVENTION.setMaturityBusinessDayConvention(MF);
    USD_FIXED_3M_EOM_CONVENTION.setRollConvention(RollConvention.EOM);
    USD_FIXED_3M_EOM_CONVENTION.setCompoundingMethod(CompoundingMethod.NONE);

    USD_LIBOR_3M_EOM_CONVENTION = new FloatingInterestRateSwapLegConvention("Test2", ExternalIdBundle.of("Scheme", "USD_LIBOR_3M FIXED"));
    USD_LIBOR_3M_EOM_CONVENTION.setDayCountConvention(DayCounts.ACT_360);
    USD_LIBOR_3M_EOM_CONVENTION.setCalculationCalendars(USNYGBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setMaturityCalendars(USNYGBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setPaymentCalendars(USNYGBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setPaymentFrequency(SimpleFrequency.QUARTERLY);
    USD_LIBOR_3M_EOM_CONVENTION.setPaymentRelativeTo(DateRelativeTo.END);
    USD_LIBOR_3M_EOM_CONVENTION.setSettlementDays(2);
    USD_LIBOR_3M_EOM_CONVENTION.setPaymentDayConvention(MF);
    USD_LIBOR_3M_EOM_CONVENTION.setCalculationBusinessDayConvention(MF);
    USD_LIBOR_3M_EOM_CONVENTION.setCalculationFrequency(SimpleFrequency.QUARTERLY);
    USD_LIBOR_3M_EOM_CONVENTION.setMaturityBusinessDayConvention(MF);
    USD_LIBOR_3M_EOM_CONVENTION.setFixingCalendars(GBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setFixingBusinessDayConvention(BusinessDayConventions.NONE);
    USD_LIBOR_3M_EOM_CONVENTION.setResetFrequency(SimpleFrequency.QUARTERLY);
    USD_LIBOR_3M_EOM_CONVENTION.setResetCalendars(USNYGBLO);
    USD_LIBOR_3M_EOM_CONVENTION.setResetBusinessDayConvention(MF);
    USD_LIBOR_3M_EOM_CONVENTION.setResetRelativeTo(DateRelativeTo.START);
    USD_LIBOR_3M_EOM_CONVENTION.setRollConvention(RollConvention.EOM);
    USD_LIBOR_3M_EOM_CONVENTION.setRateType(FloatingRateType.IBOR);
    USD_LIBOR_3M_EOM_CONVENTION.setCompoundingMethod(CompoundingMethod.NONE);

    USD_FIX_LEG = USD_FIXED_3M_EOM_CONVENTION.toLeg(InterestRateSwapNotional.of(Currency.USD, Lists.newArrayList(LocalDate.MIN, LocalDate.MAX), Lists.newArrayList(1e6, 1e5)), PayReceiveType.PAY, new Rate(0.01234));

    USD_FLOAT_LEG = USD_LIBOR_3M_EOM_CONVENTION.toLeg(new InterestRateSwapNotional(Currency.USD, 1e6), PayReceiveType.RECEIVE);
  }

  @Test
  public void testSwapSecurity() {
    final InterestRateSwapSecurity security = new InterestRateSwapSecurity(ExternalIdBundle.EMPTY, "a swap",
                                                                           LocalDate.now(), LocalDate.now(),
                                                                           Sets.newHashSet(USD_FIX_LEG, USD_FLOAT_LEG));
    assertEncodeDecodeCycle(InterestRateSwapSecurity.class, security);
  }


}
