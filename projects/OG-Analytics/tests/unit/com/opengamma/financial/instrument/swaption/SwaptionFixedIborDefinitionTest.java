package com.opengamma.financial.instrument.swaption;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.ZZZSwapFixedIborDefinition;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Tenor;

public class SwaptionFixedIborDefinitionTest {

  private static final ZonedDateTime EXPIRY_DATE = DateUtil.getUTCDate(2011, 3, 28);
  private static final boolean IS_CASH = false;
  //Swap 2Y
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Tenor ANNUITY_TENOR = new Tenor(Period.ofYears(2));
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 30);
  private static final double NOTIONAL = 1000000; //1m
  //Fixed leg: Semi-annual bond
  private static final PeriodFrequency FIXED_PAYMENT_FREQUENCY = PeriodFrequency.SEMI_ANNUAL;
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_FREQUENCY, CALENDAR, FIXED_DAY_COUNT, BUSINESS_DAY,
      IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Tenor INDEX_TENOR = new Tenor(Period.ofMonths(3));
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex INDEX = new IborIndex(INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, !FIXED_IS_PAYER);

  private static final ZZZSwapFixedIborDefinition SWAP = new ZZZSwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);

  private static final SwaptionFixedIborDefinition SWAPTION = new SwaptionFixedIborDefinition(EXPIRY_DATE, SWAP, IS_CASH);

  @Test(expected = IllegalArgumentException.class)
  public void testNullExpiryDate() {
    new SwaptionFixedIborDefinition(null, SWAP, IS_CASH);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    new SwaptionFixedIborDefinition(EXPIRY_DATE, null, IS_CASH);
  }

  @Test
  public void testGetter() {
    assertEquals(SWAPTION.getExpiryDate(), EXPIRY_DATE);
    assertEquals(SWAPTION.getUnderlyingSwap(), SWAP);
    assertEquals(SWAPTION.isCash(), IS_CASH);
  }

}
