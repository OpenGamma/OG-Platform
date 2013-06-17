/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class IborIndexVisitorTest {
  private static final IborIndex INDEX = new IborIndex(Currency.USD, Period.ofMonths(3), 0, DayCountFactory.INSTANCE.getDayCount("30/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false);
  private static final Calendar CALENDAR = new NoHolidayCalendar();

  @Test
  public void testIborDeposit() {
    final DepositIborDefinition definition = DepositIborDefinition.fromStart(DateUtils.getUTCDate(2013, 1, 1), 1, 0.02, INDEX, CALENDAR);
    final Collection<IborIndex> actual = definition.accept(IborIndexVisitor.getInstance());
    assertEquals(1, actual.size());
    assertEquals(INDEX, Iterables.getOnlyElement(actual));
  }

  @Test
  public void testFRA() {
    final ForwardRateAgreementDefinition definition = ForwardRateAgreementDefinition.from(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 4, 1), 1, INDEX, 0.02, CALENDAR);
    final Collection<IborIndex> actual = definition.accept(IborIndexVisitor.getInstance());
    assertEquals(1, actual.size());
    assertEquals(INDEX, Iterables.getOnlyElement(actual));
  }

  @Test
  public void testCash() {
    final CashDefinition definition = new CashDefinition(Currency.USD, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 2, 1), 1, 0.01, 0.1);
    final Collection<IborIndex> actual = definition.accept(IborIndexVisitor.getInstance());
    assertEquals(0, actual.size());
  }

  @Test
  public void testSwap() {
    final AnnuityCouponIborDefinition ibor = AnnuityCouponIborDefinition.from(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), Period.ofMonths(3), 1, INDEX, true,
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false, DayCountFactory.INSTANCE.getDayCount("30/360"), CALENDAR);
    final IborIndex index = new IborIndex(Currency.USD, Period.ofMonths(6), 0, DayCountFactory.INSTANCE.getDayCount("30/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false);
    final AnnuityCouponIborSpreadDefinition iborSpread = AnnuityCouponIborSpreadDefinition.from(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), Period.ofMonths(3), 1, index, false,
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false, DayCountFactory.INSTANCE.getDayCount("30/360"), 0.001, CALENDAR);
    final AnnuityCouponFixedDefinition fixed = AnnuityCouponFixedDefinition.from(Currency.USD, DateUtils.getUTCDate(2013, 1, 1), Period.ofYears(10), Period.ofMonths(3), CALENDAR,
        DayCountFactory.INSTANCE.getDayCount("30/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false, 1, 0.04, false);
    SwapDefinition definition = new SwapDefinition(ibor, fixed);
    Collection<IborIndex> actual = definition.accept(IborIndexVisitor.getInstance());
    assertEquals(1, actual.size());
    assertEquals(INDEX, Iterables.getOnlyElement(actual));
    definition = new SwapDefinition(fixed, AnnuityCouponFixedDefinition.from(Currency.USD, DateUtils.getUTCDate(2013, 1, 1), Period.ofYears(10), Period.ofMonths(3), CALENDAR,
        DayCountFactory.INSTANCE.getDayCount("30/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false, 1, 0.04, true));
    actual = definition.accept(IborIndexVisitor.getInstance());
    assertEquals(0, actual.size());
    definition = new SwapDefinition(ibor, iborSpread);
    actual = definition.accept(IborIndexVisitor.getInstance());
    assertEquals(2, actual.size());
    assertEquals(Sets.newHashSet(INDEX, index), actual);
  }
}
