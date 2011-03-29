package com.opengamma.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

public class CMSIndexTest {

  //Libor3m
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_IBOR = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_IBOR, BUSINESS_DAY, IS_EOM);
  //CMS index: CMS2YUSD3M6M - Semi-bond/Quarterly-money
  private static final Period FIXED_LEG_PERIOD = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_FIXED = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final Period CMS_TENOR = Period.ofYears(2);
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, CMS_TENOR);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedLegPeriod() {
    new CMSIndex(null, DAY_COUNT_FIXED, IBOR_INDEX, CMS_TENOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedDayCount() {
    new CMSIndex(FIXED_LEG_PERIOD, null, IBOR_INDEX, CMS_TENOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIborIndex() {
    new CMSIndex(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, null, CMS_TENOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCMSTenor() {
    new CMSIndex(FIXED_LEG_PERIOD, DAY_COUNT_FIXED, IBOR_INDEX, null);
  }

  @Test
  public void testGetter() {
    assertEquals(CMS_INDEX.getCurrency(), CUR);
    assertEquals(CMS_INDEX.getFixedLegDayCount(), DAY_COUNT_FIXED);
    assertEquals(CMS_INDEX.getFixedLegPeriod(), FIXED_LEG_PERIOD);
    assertEquals(CMS_INDEX.getIborIndex(), IBOR_INDEX);
    assertEquals(CMS_INDEX.getTenor(), CMS_TENOR);
  }
}
