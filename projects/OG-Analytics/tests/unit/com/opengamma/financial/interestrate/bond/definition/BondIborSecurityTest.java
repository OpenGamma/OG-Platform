package com.opengamma.financial.interestrate.bond.definition;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

@SuppressWarnings("unchecked")
public class BondIborSecurityTest {

  //Quarterly Libor6m 2Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/360");
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, CALENDAR, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM);
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final AnnuityCouponIborDefinition COUPON_DEFINITION = AnnuityCouponIborDefinition.fromAccrualUnadjusted(START_ACCRUAL_DATE, MATURITY_DATE, 1.0, IBOR_INDEX, false);
  private static final AnnuityPaymentFixedDefinition NOMINAL_DEFINITION = new AnnuityPaymentFixedDefinition(new PaymentFixedDefinition[] {new PaymentFixedDefinition(CUR, BUSINESS_DAY.adjustDate(
      CALENDAR, MATURITY_DATE), 1.0)});
  // to derivatives
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final ZonedDateTime STANDARD_SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double STANDARD_SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, STANDARD_SETTLEMENT_DATE);
  private static final double FIRST_FIXING = 0.02;
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String DISCOUNTING_CURVE_NAME = "Discounting";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};

  private static final AnnuityPaymentFixed NOMINAL = NOMINAL_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final DoubleTimeSeries<ZonedDateTime> FIXING_TS;
  private static final GenericAnnuity<Coupon> COUPON;
  private static final BondIborSecurity BOND_DESCRIPTION;

  static {
    final List<ZonedDateTime> fixingDates = new ArrayList<ZonedDateTime>();
    final List<Double> fixingRates = new ArrayList<Double>();
    for (int i = 0; i < COUPON_DEFINITION.getNumberOfPayments(); i++) {
      if (COUPON_DEFINITION.getNthPayment(i).getFixingDate().isBefore(REFERENCE_DATE)) {
        fixingDates.add(COUPON_DEFINITION.getNthPayment(i).getFixingDate());
        fixingRates.add(FIRST_FIXING);
      }
    }
    FIXING_TS = new ArrayZonedDateTimeDoubleTimeSeries(fixingDates, fixingRates);
    COUPON = (GenericAnnuity<Coupon>) COUPON_DEFINITION.toDerivative(REFERENCE_DATE, FIXING_TS, CURVES_NAME);
    BOND_DESCRIPTION = new BondIborSecurity(NOMINAL, COUPON, STANDARD_SETTLEMENT_TIME, DISCOUNTING_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNominal() {
    new BondIborSecurity(null, COUPON, STANDARD_SETTLEMENT_TIME, DISCOUNTING_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCoupon() {
    new BondIborSecurity(NOMINAL, null, STANDARD_SETTLEMENT_TIME, DISCOUNTING_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDISCOUNTING() {
    new BondIborSecurity(NOMINAL, COUPON, STANDARD_SETTLEMENT_TIME, null);
  }

  @Test
  public void testGetters() {
    assertEquals(NOMINAL, BOND_DESCRIPTION.getNominal());
    assertEquals(COUPON, BOND_DESCRIPTION.getCoupon());
  }
}
