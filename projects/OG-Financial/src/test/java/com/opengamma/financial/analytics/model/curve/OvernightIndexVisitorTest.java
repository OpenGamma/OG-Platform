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
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.schedule.NoHolidayCalendar;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class OvernightIndexVisitorTest {
  private static final IndexON INDEX = new IndexON("", Currency.USD, DayCountFactory.INSTANCE.getDayCount("30/360"), 0);
  private static final Calendar CALENDAR = new NoHolidayCalendar();

  @Test
  public void testSwap() {
    final AnnuityCouponONSimplifiedDefinition overnight1 = AnnuityCouponONSimplifiedDefinition.from(DateUtils.getUTCDate(2013, 1, 1), Period.ofYears(10), 1, false, INDEX, 0, CALENDAR, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), Period.ofMonths(3), false);
    final IndexON index = new IndexON("", Currency.USD, DayCountFactory.INSTANCE.getDayCount("30/360"), 1);
    final AnnuityCouponONSimplifiedDefinition overnight2 = AnnuityCouponONSimplifiedDefinition.from(DateUtils.getUTCDate(2013, 1, 1), Period.ofYears(10), 1, true, index, 0, CALENDAR, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), Period.ofMonths(3), false);
    final AnnuityCouponFixedDefinition fixed = AnnuityCouponFixedDefinition.from(Currency.USD, DateUtils.getUTCDate(2013, 1, 1), Period.ofYears(10), Period.ofMonths(3), CALENDAR,
        DayCountFactory.INSTANCE.getDayCount("30/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false, 1, 0.04, true);
    SwapDefinition definition = new SwapDefinition(overnight1, fixed);
    Collection<IndexON> actual = definition.accept(OvernightIndexVisitor.getInstance());
    assertEquals(1, actual.size());
    assertEquals(INDEX, Iterables.getOnlyElement(actual));
    definition = new SwapDefinition(fixed, AnnuityCouponFixedDefinition.from(Currency.USD, DateUtils.getUTCDate(2013, 1, 1), Period.ofYears(10), Period.ofMonths(3), CALENDAR,
        DayCountFactory.INSTANCE.getDayCount("30/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false, 1, 0.04, false));
    actual = definition.accept(OvernightIndexVisitor.getInstance());
    assertEquals(0, actual.size());
    definition = new SwapDefinition(overnight1, overnight2);
    actual = definition.accept(OvernightIndexVisitor.getInstance());
    assertEquals(2, actual.size());
    assertEquals(Sets.newHashSet(INDEX, index), actual);
  }

}
