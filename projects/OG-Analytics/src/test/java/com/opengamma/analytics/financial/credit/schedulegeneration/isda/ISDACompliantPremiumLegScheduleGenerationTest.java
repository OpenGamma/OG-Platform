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
 * 
 */
public class ISDACompliantPremiumLegScheduleGenerationTest {
  private static final GenerateCreditDefaultSwapPremiumLegSchedule DEPRECATED_CALCULATOR = new GenerateCreditDefaultSwapPremiumLegSchedule();
  private static final GenerateCreditDefaultSwapPremiumLegScheduleNew CALCULATOR = new GenerateCreditDefaultSwapPremiumLegScheduleNew();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 1, 6);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCDS() {
    CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(null);
  }

  @Test
  public void regressionTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] deprecatedResult = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime[] result = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(deprecatedResult, result);
  }

  @Test
  public void testStartDateEqualsMaturityDate() {
    CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithProtectionStart(true);
    cds = cds.withMaturityDate(cds.getEffectiveDate());
    assertDateArrayEquals(new ZonedDateTime[] {cds.getStartDate(), cds.getStartDate().plusDays(1) }, CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
  }

  @Test
  public void testOneMonthQuarterlyCDS() {

  }

  @Test
  public void testFrontShort() {
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTSHORT);
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
    ZonedDateTime[] expected = dates.toArray(new ZonedDateTime[dates.size()]);
    ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTSHORT);
    final BusinessDayConvention bdc = cds.getBusinessDayAdjustmentConvention();
    final Calendar holidays = cds.getCalendar();
    date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(bdc.adjustDate(holidays, date));
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    expected = dates.toArray(new ZonedDateTime[dates.size()]);
    actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
  }

  @Test
  public void testFrontShortFirstDateHoliday() {
    final ZonedDateTime startDate = new IMMDates(2008).getImmDateSeptember();
    final ZonedDateTime effectiveDate = startDate.plusDays(1);
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTSHORT);
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
    ZonedDateTime[] expected = dates.toArray(new ZonedDateTime[dates.size()]);
    ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTSHORT);
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
    expected = dates.toArray(new ZonedDateTime[dates.size()]);
    actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
  }

  @Test
  public void testFrontLong() {
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTLONG);
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
    ZonedDateTime[] expected = dates.toArray(new ZonedDateTime[dates.size()]);
    ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTSHORT);
    final BusinessDayConvention bdc = cds.getBusinessDayAdjustmentConvention();
    final Calendar holidays = cds.getCalendar();
    date = new IMMDates(cds.getStartDate().getYear()).getImmDateDecember();
    dates = new ArrayList<>();
    dates.add(cds.getStartDate());
    while (!date.isAfter(cds.getMaturityDate())) {
      dates.add(bdc.adjustDate(holidays, date));
      date = date.plusMonths(cds.getCouponFrequency().getPeriod().toTotalMonths());
    }
    expected = dates.toArray(new ZonedDateTime[dates.size()]);
    actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
  }

  @Test
  public void testFrontLongFirstDateHoliday() {
    final ZonedDateTime startDate = new IMMDates(2008).getImmDateSeptember();
    final ZonedDateTime effectiveDate = startDate.plusDays(1);
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTLONG);
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
    ZonedDateTime[] expected = dates.toArray(new ZonedDateTime[dates.size()]);
    ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTLONG);
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
    expected = dates.toArray(new ZonedDateTime[dates.size()]);
    actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
  }

  @Test
  public void testFrontOneDate() {
    final ZonedDateTime startDate = new IMMDates(2007).getImmDateJune();
    final ZonedDateTime effectiveDate = startDate.plusDays(1);
    final ZonedDateTime endDate = startDate.plusMonths(1);
    LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinitionWithStubType(StubType.FRONTLONG);
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
    final ZonedDateTime[] expected = new ZonedDateTime[] {startDate, endDate };
    final ZonedDateTime[] actual = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds));
  }

  //TODO test short dates

  @Test(enabled = false)
  public void timeBDeprecated() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 1000000; i++) {
      DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated:\t" + (endTime - startTime) / j * 100);
  }

  @Test(enabled = false)
  public void timeARefactored() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 1000000; i++) {
      CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored:\t" + (endTime - startTime) / j * 100);
  }

}
