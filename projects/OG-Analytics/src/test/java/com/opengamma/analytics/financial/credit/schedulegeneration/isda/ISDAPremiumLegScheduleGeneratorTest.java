/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import static com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleTestUtils.assertDateArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.schedulegeneration.IMMDates;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

/**
 * We are testing the schedule generated for ISDA premium leg. There is 4 schedule generated.
 */
public class ISDAPremiumLegScheduleGeneratorTest {
  private static final GenerateCreditDefaultSwapPremiumLegSchedule DEPRECATED_CALCULATOR = new GenerateCreditDefaultSwapPremiumLegSchedule();
  //we don't want to test CALCULATOR in this file (for the moment ???), that's why everywhere CALCULATOR appears its in comment.
  /* private static final ISDAPremiumLegScheduleGenerator CALCULATOR = new ISDAPremiumLegScheduleGenerator();*/
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 1, 6);

  /* @Test(expectedExceptions = IllegalArgumentException.class)
   public void testNullCDS() {
     CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(null);
   }*/

  /*@Test
  public void regressionTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] deprecatedResult = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime[] result = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(deprecatedResult, result);
  }*/

  /*@Test
  public void testStartDateEqualsMaturityDate() {
    CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(true);
    cds = cds.withMaturityDate(cds.getEffectiveDate());
    assertDateArrayEquals(new ZonedDateTime[] {cds.getStartDate(), cds.getStartDate().plusDays(1) }, CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
  }*/

  @Test
  public void testOneMonthQuarterlyCDS() {

  }

  /*
   * Test of the schedule generation, here we test the FRONTSHORT stub case : 
   * Stub is at the start (front) of the cashflow schedule; first coupon is on the first IMM date after the effective date (short stub).
   */
  @Test
  public void testFrontShort() {
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTSHORT);
    cds = new LegacyVanillaCreditDefaultSwapDefinition(cds.getBuySellProtection(),
        cds.getProtectionBuyer(),
        cds.getProtectionSeller(),
        cds.getReferenceEntity(),
        cds.getCurrency(),
        cds.getDebtSeniority(),
        cds.getRestructuringClause(),
        new NoHolidayCalendar(),
        cds.getStartDate(),
        cds.getEffectiveDate(),
        cds.getMaturityDate(),
        cds.getStubType(),
        cds.getCouponFrequency(),
        cds.getDayCountFractionConvention(),
        cds.getBusinessDayAdjustmentConvention(),
        cds.getIMMAdjustMaturityDate(),
        cds.getAdjustEffectiveDate(),
        cds.getAdjustMaturityDate(),
        cds.getNotional(),
        cds.getRecoveryRate(),
        cds.getIncludeAccruedPremium(),
        cds.getProtectionStart(),
        cds.getParSpread());
    ZonedDateTime date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    List<ZonedDateTime> dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(date);
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    ZonedDateTime[] expected1 = dates.toArray(new ZonedDateTime[dates.size()]);
    ZonedDateTime[] expected2 = new ZonedDateTime[dates.size()];
    ZonedDateTime[] expected3 = new ZonedDateTime[dates.size()];
    ZonedDateTime[] expected4 = new ZonedDateTime[dates.size()];
    /*ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);*/
    ZonedDateTime[][] calculatedSchedule = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    ZonedDateTime[] extractCalculatedSchedule1 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedSchedule2 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedSchedule3 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedSchedule4 = new ZonedDateTime[calculatedSchedule.length];
    for (int loop = 0; loop < calculatedSchedule.length; loop++) {
      extractCalculatedSchedule1[loop] = calculatedSchedule[loop][0];
    }

    for (int loop = 1; loop < calculatedSchedule.length; loop++) {
      extractCalculatedSchedule2[loop] = calculatedSchedule[loop][1];
      extractCalculatedSchedule3[loop] = calculatedSchedule[loop][2];
      extractCalculatedSchedule4[loop] = calculatedSchedule[loop][3];
    }
    ZonedDateTime prevDate = expected1[0];
    ZonedDateTime prevDateAdj = prevDate;
    for (int loop = 1; loop < calculatedSchedule.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }

    assertDateArrayEquals(expected1, extractCalculatedSchedule1);
    assertDateArrayEquals(expected2, extractCalculatedSchedule2);
    assertDateArrayEquals(expected3, extractCalculatedSchedule3);
    assertDateArrayEquals(expected4, extractCalculatedSchedule4);

    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTSHORT);
    final BusinessDayConvention bdc = cds.getBusinessDayAdjustmentConvention();
    final Calendar holidays = cds.getCalendar();
    date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(bdc.adjustDate(holidays, date));
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    expected1 = dates.toArray(new ZonedDateTime[dates.size()]);
    //CALCULATOR not tested in this file
    /* actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
     assertDateArrayEquals(expected, actual);*/

    ZonedDateTime[][] calculatedScheduleAdjusted = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    ZonedDateTime[] extractCalculatedScheduleAdjusted1 = new ZonedDateTime[calculatedScheduleAdjusted.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted2 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted3 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted4 = new ZonedDateTime[calculatedSchedule.length];
    for (int loop = 0; loop < calculatedSchedule.length; loop++) {
      extractCalculatedScheduleAdjusted1[loop] = calculatedScheduleAdjusted[loop][0];
    }

    for (int loop = 1; loop < calculatedSchedule.length; loop++) {
      extractCalculatedScheduleAdjusted2[loop] = calculatedScheduleAdjusted[loop][1];
      extractCalculatedScheduleAdjusted3[loop] = calculatedScheduleAdjusted[loop][2];
      extractCalculatedScheduleAdjusted4[loop] = calculatedScheduleAdjusted[loop][3];
    }

    prevDate = expected1[0];
    prevDateAdj = prevDate;
    for (int loop = 1; loop < calculatedSchedule.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }

    assertDateArrayEquals(expected1, extractCalculatedScheduleAdjusted1);
    assertDateArrayEquals(expected2, extractCalculatedScheduleAdjusted2);
    assertDateArrayEquals(expected3, extractCalculatedScheduleAdjusted3);
    assertDateArrayEquals(expected4, extractCalculatedScheduleAdjusted4);
  }

  /*
   * Test of the schedule generation, here we test the FRONTSHORT stub case when the start date is an holiday: 
   * Stub is at the start (front) of the cashflow schedule; first coupon is on the first IMM date after the effective date (short stub).
   */
  @Test
  public void testFrontShortFirstDateHoliday() {
    final ZonedDateTime startDate = new IMMDates(2008).getImmDateSeptember();
    final ZonedDateTime effectiveDate = startDate.plusDays(1);
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTSHORT);
    cds = new LegacyVanillaCreditDefaultSwapDefinition(cds.getBuySellProtection(),
        cds.getProtectionBuyer(),
        cds.getProtectionSeller(),
        cds.getReferenceEntity(),
        cds.getCurrency(),
        cds.getDebtSeniority(),
        cds.getRestructuringClause(),
        new NoHolidayCalendar(),
        startDate,
        effectiveDate,
        cds.getMaturityDate(),
        cds.getStubType(),
        cds.getCouponFrequency(),
        cds.getDayCountFractionConvention(),
        cds.getBusinessDayAdjustmentConvention(),
        cds.getIMMAdjustMaturityDate(),
        cds.getAdjustEffectiveDate(),
        cds.getAdjustMaturityDate(),
        cds.getNotional(),
        cds.getRecoveryRate(),
        cds.getIncludeAccruedPremium(),
        cds.getProtectionStart(),
        cds.getParSpread());
    ZonedDateTime date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    List<ZonedDateTime> dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(date);
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    ZonedDateTime[] expected1 = dates.toArray(new ZonedDateTime[dates.size()]);
    ZonedDateTime[] expected2 = new ZonedDateTime[dates.size()];
    ZonedDateTime[] expected3 = new ZonedDateTime[dates.size()];
    ZonedDateTime[] expected4 = new ZonedDateTime[dates.size()];

    //CALCULATOR not tested in this file
    /*ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);*/
    ZonedDateTime[][] calculatedSchedule = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    ZonedDateTime[] extractCalculatedSchedule1 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedSchedule2 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedSchedule3 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedSchedule4 = new ZonedDateTime[calculatedSchedule.length];
    for (int loop = 0; loop < calculatedSchedule.length; loop++) {
      extractCalculatedSchedule1[loop] = calculatedSchedule[loop][0];
    }

    for (int loop = 1; loop < calculatedSchedule.length; loop++) {
      extractCalculatedSchedule2[loop] = calculatedSchedule[loop][1];
      extractCalculatedSchedule3[loop] = calculatedSchedule[loop][2];
      extractCalculatedSchedule4[loop] = calculatedSchedule[loop][3];
    }
    ZonedDateTime prevDate = expected1[0];
    ZonedDateTime prevDateAdj = prevDate;
    for (int loop = 1; loop < calculatedSchedule.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }

    assertDateArrayEquals(expected1, extractCalculatedSchedule1);
    assertDateArrayEquals(expected2, extractCalculatedSchedule2);
    assertDateArrayEquals(expected3, extractCalculatedSchedule3);
    assertDateArrayEquals(expected4, extractCalculatedSchedule4);

    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTSHORT);
    cds = new LegacyVanillaCreditDefaultSwapDefinition(cds.getBuySellProtection(),
        cds.getProtectionBuyer(),
        cds.getProtectionSeller(),
        cds.getReferenceEntity(),
        cds.getCurrency(),
        cds.getDebtSeniority(),
        cds.getRestructuringClause(),
        cds.getCalendar(),
        startDate,
        effectiveDate,
        cds.getMaturityDate(),
        cds.getStubType(),
        cds.getCouponFrequency(),
        cds.getDayCountFractionConvention(),
        cds.getBusinessDayAdjustmentConvention(),
        cds.getIMMAdjustMaturityDate(),
        cds.getAdjustEffectiveDate(),
        cds.getAdjustMaturityDate(),
        cds.getNotional(),
        cds.getRecoveryRate(),
        cds.getIncludeAccruedPremium(),
        cds.getProtectionStart(),
        cds.getParSpread());
    final BusinessDayConvention bdc = cds.getBusinessDayAdjustmentConvention();
    final Calendar holidays = cds.getCalendar();
    date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(bdc.adjustDate(holidays, date));
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    expected1 = dates.toArray(new ZonedDateTime[dates.size()]);
    expected2 = new ZonedDateTime[dates.size()];
    expected3 = new ZonedDateTime[dates.size()];
    expected4 = new ZonedDateTime[dates.size()];

    //we don't want to test CALCULATOR in this file (for the moment ???)
    /* actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
     assertDateArrayEquals(expected, actual);*/
    ZonedDateTime[][] calculatedScheduleAdjusted = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    ZonedDateTime[] extractCalculatedScheduleAdjusted1 = new ZonedDateTime[calculatedScheduleAdjusted.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted2 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted3 = new ZonedDateTime[calculatedSchedule.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted4 = new ZonedDateTime[calculatedSchedule.length];
    for (int loop = 0; loop < calculatedSchedule.length; loop++) {
      extractCalculatedScheduleAdjusted1[loop] = calculatedScheduleAdjusted[loop][0];
    }

    for (int loop = 1; loop < calculatedSchedule.length; loop++) {
      extractCalculatedScheduleAdjusted2[loop] = calculatedScheduleAdjusted[loop][1];
      extractCalculatedScheduleAdjusted3[loop] = calculatedScheduleAdjusted[loop][2];
      extractCalculatedScheduleAdjusted4[loop] = calculatedScheduleAdjusted[loop][3];
    }

    prevDate = expected1[0];
    prevDateAdj = prevDate;
    for (int loop = 1; loop < calculatedSchedule.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }

    assertDateArrayEquals(expected1, extractCalculatedScheduleAdjusted1);
    assertDateArrayEquals(expected2, extractCalculatedScheduleAdjusted2);
    assertDateArrayEquals(expected3, extractCalculatedScheduleAdjusted3);
    assertDateArrayEquals(expected4, extractCalculatedScheduleAdjusted4);
  }

  /*
   * Test of the schedule generation, here we test the FRONTLONG stub case: 
   * Stub is at the end (back) of the cashflow schedule; last but one coupon is on the last scheduled coupon date before the maturity date (short stub)
   */
  @Test
  public void testFrontLong() {
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTLONG);
    cds = new LegacyVanillaCreditDefaultSwapDefinition(cds.getBuySellProtection(),
        cds.getProtectionBuyer(),
        cds.getProtectionSeller(),
        cds.getReferenceEntity(),
        cds.getCurrency(),
        cds.getDebtSeniority(),
        cds.getRestructuringClause(),
        new NoHolidayCalendar(),
        cds.getStartDate(),
        cds.getEffectiveDate(),
        cds.getMaturityDate(),
        cds.getStubType(),
        cds.getCouponFrequency(),
        cds.getDayCountFractionConvention(),
        cds.getBusinessDayAdjustmentConvention(),
        cds.getIMMAdjustMaturityDate(),
        cds.getAdjustEffectiveDate(),
        cds.getAdjustMaturityDate(),
        cds.getNotional(),
        cds.getRecoveryRate(),
        cds.getIncludeAccruedPremium(),
        cds.getProtectionStart(),
        cds.getParSpread());
    ZonedDateTime date = new IMMDates(cds.getStartDate().getYear()).getImmDateNextMarch();
    List<ZonedDateTime> dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(date);
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    ZonedDateTime[] expected1 = dates.toArray(new ZonedDateTime[dates.size()]);
    ZonedDateTime[] expected2 = new ZonedDateTime[dates.size()];
    ZonedDateTime[] expected3 = new ZonedDateTime[dates.size()];
    ZonedDateTime[] expected4 = new ZonedDateTime[dates.size()];
    /*ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);*/
    ZonedDateTime[][] calculated = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    ZonedDateTime[] extractCalculatedSchedule1 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule2 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule3 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule4 = new ZonedDateTime[calculated.length];
    for (int loop = 0; loop < calculated.length; loop++) {
      extractCalculatedSchedule1[loop] = calculated[loop][0];
    }

    for (int loop = 1; loop < calculated.length; loop++) {
      extractCalculatedSchedule2[loop] = calculated[loop][1];
      extractCalculatedSchedule3[loop] = calculated[loop][2];
      extractCalculatedSchedule4[loop] = calculated[loop][3];
    }
    ZonedDateTime prevDate = expected1[0];
    ZonedDateTime prevDateAdj = prevDate;
    for (int loop = 1; loop < calculated.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }

    assertDateArrayEquals(expected1, extractCalculatedSchedule1);
    assertDateArrayEquals(expected2, extractCalculatedSchedule2);
    assertDateArrayEquals(expected3, extractCalculatedSchedule3);
    assertDateArrayEquals(expected4, extractCalculatedSchedule4);

    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTSHORT);
    final BusinessDayConvention bdc = cds.getBusinessDayAdjustmentConvention();
    final Calendar holidays = cds.getCalendar();
    date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(bdc.adjustDate(holidays, date));
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    expected1 = dates.toArray(new ZonedDateTime[dates.size()]);
    expected2 = new ZonedDateTime[dates.size()];
    expected3 = new ZonedDateTime[dates.size()];
    expected4 = new ZonedDateTime[dates.size()];
    /*actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);*/
    ZonedDateTime[][] calculatedAdjusated = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    ZonedDateTime[] extractCalculatedScheduleAdjusted1 = new ZonedDateTime[calculatedAdjusated.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted2 = new ZonedDateTime[calculatedAdjusated.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted3 = new ZonedDateTime[calculatedAdjusated.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted4 = new ZonedDateTime[calculatedAdjusated.length];
    for (int loop = 0; loop < calculatedAdjusated.length; loop++) {
      extractCalculatedScheduleAdjusted1[loop] = calculatedAdjusated[loop][0];
    }

    for (int loop = 1; loop < calculatedAdjusated.length; loop++) {
      extractCalculatedScheduleAdjusted2[loop] = calculatedAdjusated[loop][1];
      extractCalculatedScheduleAdjusted3[loop] = calculatedAdjusated[loop][2];
      extractCalculatedScheduleAdjusted4[loop] = calculatedAdjusated[loop][3];
    }

    prevDate = expected1[0];
    prevDateAdj = prevDate;
    for (int loop = 1; loop < calculatedAdjusated.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }

    assertDateArrayEquals(expected1, extractCalculatedScheduleAdjusted1);
    assertDateArrayEquals(expected2, extractCalculatedScheduleAdjusted2);
    assertDateArrayEquals(expected3, extractCalculatedScheduleAdjusted3);
    assertDateArrayEquals(expected4, extractCalculatedScheduleAdjusted4);
  }

  /*
   * Test of the schedule generation, here we test the FRONTLONG stub case when the start date is an holiday: 
   * Stub is at the end (back) of the cashflow schedule; last but one coupon is on the last scheduled coupon date before the maturity date (short stub)
   */
  @Test
  public void testFrontLongFirstDateHoliday() {
    final ZonedDateTime startDate = new IMMDates(2008).getImmDateSeptember();
    final ZonedDateTime effectiveDate = startDate.plusDays(1);
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTLONG);
    cds = new LegacyVanillaCreditDefaultSwapDefinition(cds.getBuySellProtection(),
        cds.getProtectionBuyer(),
        cds.getProtectionSeller(),
        cds.getReferenceEntity(),
        cds.getCurrency(),
        cds.getDebtSeniority(),
        cds.getRestructuringClause(),
        new NoHolidayCalendar(),
        startDate,
        effectiveDate,
        cds.getMaturityDate(),
        cds.getStubType(),
        cds.getCouponFrequency(),
        cds.getDayCountFractionConvention(),
        cds.getBusinessDayAdjustmentConvention(),
        cds.getIMMAdjustMaturityDate(),
        cds.getAdjustEffectiveDate(),
        cds.getAdjustMaturityDate(),
        cds.getNotional(),
        cds.getRecoveryRate(),
        cds.getIncludeAccruedPremium(),
        cds.getProtectionStart(),
        cds.getParSpread());
    ZonedDateTime date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    List<ZonedDateTime> dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(date);
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    ZonedDateTime[] expected1 = dates.toArray(new ZonedDateTime[dates.size()]);
    ZonedDateTime[] expected2 = new ZonedDateTime[dates.size()];
    ZonedDateTime[] expected3 = new ZonedDateTime[dates.size()];
    ZonedDateTime[] expected4 = new ZonedDateTime[dates.size()];
    /*ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);*/
    ZonedDateTime[][] calculated = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);

    ZonedDateTime[] extractCalculatedSchedule1 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule2 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule3 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule4 = new ZonedDateTime[calculated.length];
    for (int loop = 0; loop < calculated.length; loop++) {
      extractCalculatedSchedule1[loop] = calculated[loop][0];
    }

    for (int loop = 1; loop < calculated.length; loop++) {
      extractCalculatedSchedule2[loop] = calculated[loop][1];
      extractCalculatedSchedule3[loop] = calculated[loop][2];
      extractCalculatedSchedule4[loop] = calculated[loop][3];
    }

    ZonedDateTime prevDate = expected1[0];

    ZonedDateTime prevDateAdj = prevDate;
    for (int loop = 1; loop < calculated.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }

    assertDateArrayEquals(expected1, extractCalculatedSchedule1);
    assertDateArrayEquals(expected2, extractCalculatedSchedule2);
    assertDateArrayEquals(expected3, extractCalculatedSchedule3);
    assertDateArrayEquals(expected4, extractCalculatedSchedule4);

    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTLONG);
    cds = new LegacyVanillaCreditDefaultSwapDefinition(cds.getBuySellProtection(),
        cds.getProtectionBuyer(),
        cds.getProtectionSeller(),
        cds.getReferenceEntity(),
        cds.getCurrency(),
        cds.getDebtSeniority(),
        cds.getRestructuringClause(),
        cds.getCalendar(),
        startDate,
        effectiveDate,
        cds.getMaturityDate(),
        cds.getStubType(),
        cds.getCouponFrequency(),
        cds.getDayCountFractionConvention(),
        cds.getBusinessDayAdjustmentConvention(),
        cds.getIMMAdjustMaturityDate(),
        cds.getAdjustEffectiveDate(),
        cds.getAdjustMaturityDate(),
        cds.getNotional(),
        cds.getRecoveryRate(),
        cds.getIncludeAccruedPremium(),
        cds.getProtectionStart(),
        cds.getParSpread());
    final BusinessDayConvention bdc = cds.getBusinessDayAdjustmentConvention();
    final Calendar holidays = cds.getCalendar();
    date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(bdc.adjustDate(holidays, date));
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    expected1 = dates.toArray(new ZonedDateTime[dates.size()]);
    expected2 = new ZonedDateTime[dates.size()];
    expected3 = new ZonedDateTime[dates.size()];
    expected4 = new ZonedDateTime[dates.size()];
    /*actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);*/
    ZonedDateTime[][] calculatedAdjusted = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    ZonedDateTime[] extractCalculatedScheduleAdjusted1 = new ZonedDateTime[calculatedAdjusted.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted2 = new ZonedDateTime[calculatedAdjusted.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted3 = new ZonedDateTime[calculatedAdjusted.length];
    ZonedDateTime[] extractCalculatedScheduleAdjusted4 = new ZonedDateTime[calculatedAdjusted.length];
    for (int loop = 0; loop < calculatedAdjusted.length; loop++) {
      extractCalculatedScheduleAdjusted1[loop] = calculatedAdjusted[loop][0];
    }

    for (int loop = 1; loop < calculatedAdjusted.length; loop++) {
      extractCalculatedScheduleAdjusted2[loop] = calculatedAdjusted[loop][1];
      extractCalculatedScheduleAdjusted3[loop] = calculatedAdjusted[loop][2];
      extractCalculatedScheduleAdjusted4[loop] = calculatedAdjusted[loop][3];
    }

    prevDate = expected1[0];
    prevDateAdj = prevDate;
    for (int loop = 1; loop < calculatedAdjusted.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }

    assertDateArrayEquals(expected1, extractCalculatedScheduleAdjusted1);
    assertDateArrayEquals(expected2, extractCalculatedScheduleAdjusted2);
    assertDateArrayEquals(expected3, extractCalculatedScheduleAdjusted3);
    assertDateArrayEquals(expected4, extractCalculatedScheduleAdjusted4);
  }

  /*
   * Test of the schedule generation, here we test the FRONTLONG stub case when there is only one date generated in the schedule (the schedule contain two dates but the start date is not generated): 
   * Stub is at the end (back) of the cashflow schedule; last but one coupon is on the last scheduled coupon date before the maturity date (short stub)
   */
  @Test
  public void testFrontOneDate() {
    final ZonedDateTime startDate = new IMMDates(2007).getImmDateJune();
    final ZonedDateTime effectiveDate = startDate.plusDays(1);
    final ZonedDateTime endDate = startDate.plusMonths(1);
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithStubType(StubType.FRONTLONG);
    cds = new LegacyVanillaCreditDefaultSwapDefinition(cds.getBuySellProtection(),
        cds.getProtectionBuyer(),
        cds.getProtectionSeller(),
        cds.getReferenceEntity(),
        cds.getCurrency(),
        cds.getDebtSeniority(),
        cds.getRestructuringClause(),
        new NoHolidayCalendar(),
        startDate,
        effectiveDate,
        endDate,
        cds.getStubType(),
        cds.getCouponFrequency(),
        cds.getDayCountFractionConvention(),
        cds.getBusinessDayAdjustmentConvention(),
        cds.getIMMAdjustMaturityDate(),
        cds.getAdjustEffectiveDate(),
        cds.getAdjustMaturityDate(),
        cds.getNotional(),
        cds.getRecoveryRate(),
        cds.getIncludeAccruedPremium(),
        cds.getProtectionStart(),
        cds.getParSpread());
    ZonedDateTime date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    final List<ZonedDateTime> dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(date);
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    final ZonedDateTime[] expected1 = new ZonedDateTime[] {startDate, endDate };
    ZonedDateTime[] expected2 = new ZonedDateTime[2];
    ZonedDateTime[] expected3 = new ZonedDateTime[2];
    ZonedDateTime[] expected4 = new ZonedDateTime[2];
    /*final ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);*/
    ZonedDateTime[][] calculated = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    ZonedDateTime[] extractCalculatedSchedule1 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule2 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule3 = new ZonedDateTime[calculated.length];
    ZonedDateTime[] extractCalculatedSchedule4 = new ZonedDateTime[calculated.length];
    for (int loop = 0; loop < calculated.length; loop++) {
      extractCalculatedSchedule1[loop] = calculated[loop][0];
    }

    for (int loop = 1; loop < calculated.length; loop++) {
      extractCalculatedSchedule2[loop] = calculated[loop][1];
      extractCalculatedSchedule3[loop] = calculated[loop][2];
      extractCalculatedSchedule4[loop] = calculated[loop][3];
    }

    ZonedDateTime prevDate = expected1[0];

    ZonedDateTime prevDateAdj = prevDate;
    for (int loop = 1; loop < calculated.length; loop++) {
      ZonedDateTime nextDate = expected1[loop];
      ZonedDateTime nextDateAdj = expected1[loop];
      // accStartDate
      expected2[loop] = prevDateAdj;

      // accEndDate
      expected3[loop] = nextDateAdj;

      // payDate
      expected4[loop] = nextDateAdj;

      prevDate = nextDate;
      prevDateAdj = nextDateAdj;
    }

    if (cds.getProtectionStart()) {
      expected3[expected1.length - 1] = prevDate.plusDays(1);
    }
    else
    {
      expected3[expected1.length - 1] = prevDate;
    }
    assertDateArrayEquals(expected1, extractCalculatedSchedule1);
    assertDateArrayEquals(expected2, extractCalculatedSchedule2);
    assertDateArrayEquals(expected3, extractCalculatedSchedule3);
    assertDateArrayEquals(expected4, extractCalculatedSchedule4);
  }

  //TODO test short dates

  @Test(enabled = false)
  public void timeBDeprecated() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 1000000; i++) {
      DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated:\t" + (endTime - startTime) / j * 100);
  }

  /*@Test(enabled = false)
  public void timeARefactored() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 1000000; i++) {
      CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored:\t" + (endTime - startTime) / j * 100);
  }*/

}
