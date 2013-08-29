/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDACDSCoupon;
import com.opengamma.analytics.financial.credit.cds.ISDACDSPremium;
import com.opengamma.analytics.financial.instrument.Convention;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.ActualActualICMA;
import com.opengamma.financial.convention.daycount.ActualActualICMANormal;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * ISDA definition for a CDS premium (i.e. a stream of ISDA CDS coupon payments).
 *
 * This class encodes only the structure of the payment schedule, it does not represent
 * survival probabilities.
 *
 * Note the dates recorded for accrual period start and end are not offset as per ISDA,
 * they are the actual start and end dates. Instead, offsetting happens when the ISDA
 * method is applied.
 *
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 *
 * @see ISDACDSDefinition
 * @see AnnuityCouponFixedDefinition
 */
public class ISDACDSPremiumDefinition extends AnnuityCouponFixedDefinition {

  /**
   * @param payments The payments
   * @param calendar The calendar
   */
  public ISDACDSPremiumDefinition(final ISDACDSCouponDefinition[] payments, final Calendar calendar) {
    super(payments, calendar);
  }

  /**
   * An ISDA-compliant annuity builder for a CDS contract
   *
   * @param startDate The (original unadjusted) start of the CDS contract
   * @param maturity The (unadjusted) maturity date
   * @param frequency The payment frequency
   * @param convention The convention data
   * @param stubType The stub type
   * @param protectStart Whether the start date is protected
   * @param notional The notional
   * @param spread The spread (coupon rate)
   * @param currency The currency
   * @param calendar The calendar
   * @return An ISDA-compliant definition for the CDS premium
   */
  public static ISDACDSPremiumDefinition from(final ZonedDateTime startDate, final ZonedDateTime maturity,
      final Frequency frequency, final Convention convention, final StubType stubType, final boolean protectStart,
      final double notional, final double spread, final Currency currency, final Calendar calendar) {

    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(startDate, "CDS start date");
    ArgumentChecker.notNull(maturity, "CDS maturity");
    ArgumentChecker.notNull(frequency, "frequency");
    ArgumentChecker.notNull(convention, "convention");

    final DayCount dayCount = convention.getDayCount();
    ArgumentChecker.isTrue(!(dayCount instanceof ActualActualICMA) | !(dayCount instanceof ActualActualICMANormal), "Coupon per year required for Actual Actual ICMA");

    // TODO: Handle stubType == StubType.NONE
    final boolean isStubShort = stubType == StubType.SHORT_START || stubType == StubType.SHORT_END;
    final boolean isStubAtEnd = stubType == StubType.SHORT_END || stubType == StubType.LONG_END;

    // If the stub is at the end of the schedule, compute the schedule from the beginning, and vice versa
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(startDate, maturity, frequency,
        isStubShort, !isStubAtEnd, convention.getBusinessDayConvention(), convention.getWorkingDayCalendar(), /* EOM */ false);

    final ZonedDateTime maturityWithOffset = protectStart ? maturity.plusDays(1) : maturity;

    final ISDACDSCouponDefinition[] coupons = new ISDACDSCouponDefinition[paymentDates.length];
    final int maturityIndex = coupons.length - 1;

    if (maturityIndex > 0) {

      // accrual start date is not adjusted for the first coupon
      coupons[0] = new ISDACDSCouponDefinition(currency, paymentDates[0], startDate, paymentDates[0],
          dayCount.getDayCountFraction(startDate, paymentDates[0]), notional, spread);

      for (int i = 1; i < maturityIndex; i++) {
        coupons[i] = new ISDACDSCouponDefinition(currency, paymentDates[i], paymentDates[i - 1], paymentDates[i],
            dayCount.getDayCountFraction(paymentDates[i - 1], paymentDates[i]), notional, spread);
      }

      // Accrual end date is not adjusted for the last coupon
      coupons[maturityIndex] =  new ISDACDSCouponDefinition(currency, paymentDates[maturityIndex], paymentDates[maturityIndex - 1], maturity,
          dayCount.getDayCountFraction(paymentDates[maturityIndex - 1], maturityWithOffset), notional, spread);

    } else {

      // For a premium consisting of a single payment, neither the accrual start nor end date is adjusted
      coupons[0] = new ISDACDSCouponDefinition(currency, paymentDates[0], startDate, maturity,
          dayCount.getDayCountFraction(startDate, maturityWithOffset), notional, spread);
    }

    return new ISDACDSPremiumDefinition(coupons, calendar);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not use yield curve names
   */
  @Deprecated
  @Override
  public ISDACDSPremium toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final List<ISDACDSCoupon> resultList = new ArrayList<>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getNthPayment(loopcoupon).getPaymentDate())) {
        resultList.add(((ISDACDSCouponDefinition) getNthPayment(loopcoupon)).toDerivative(date, yieldCurveNames));
      }
    }
    return new ISDACDSPremium(resultList.toArray(new ISDACDSCoupon[resultList.size()]));
  }

  @Override
  public ISDACDSPremium toDerivative(final ZonedDateTime date) {
    final List<ISDACDSCoupon> resultList = new ArrayList<>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getNthPayment(loopcoupon).getPaymentDate())) {
        resultList.add(((ISDACDSCouponDefinition) getNthPayment(loopcoupon)).toDerivative(date));
      }
    }
    return new ISDACDSPremium(resultList.toArray(new ISDACDSCoupon[resultList.size()]));
  }
}
