/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDACDSCoupon;
import com.opengamma.analytics.financial.credit.cds.ISDACDSPremium;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.ActualActualICMA;
import com.opengamma.financial.convention.daycount.ActualActualICMANormal;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Definition for the CDS premium payment
 * 
 * @author Martin Traverse, Niels Stchedroff (Riskcare)
 * 
 * @see CDSDefinition
 * @see AnnuityCouponFixedDefinition
 */
public class CDSPremiumDefinition extends AnnuityCouponFixedDefinition {
  
  public CDSPremiumDefinition(CDSCouponDefinition[] payments) {
    super(payments);
  }

  /**
   * Annuity builder from the conventions and common characteristics. The accrual dates are unadjusted. Often used for bonds.
   * @param currency The annuity currency.
   * @param cdsStartDate The (original unadjusted) start of the CDS contract
   * @param cdsMaturityDate The (unadjusted) maturity date of the annuity.
   * @param frequency The payment frequency
   * @param calendar The calendar.
   * @param dayCount The day count.
   * @param businessDay The business day convention.
   * @param notional The notional.
   * @param fixedRate The fixed rate.
   * @param protectStart
   * @return The fixed annuity.
   */
  public static CDSPremiumDefinition fromISDA(final Currency currency, final ZonedDateTime startDate, final ZonedDateTime maturity, final Frequency frequency,
    final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final double notional, final double fixedRate, final boolean protectStart) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(startDate, "CDS start date");
    ArgumentChecker.notNull(maturity, "CDS maturity");
    ArgumentChecker.notNull(frequency, "frequency");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    ArgumentChecker.isTrue(!(dayCount instanceof ActualActualICMA) | !(dayCount instanceof ActualActualICMANormal), "Coupon per year required for Actua lActual ICMA");
    
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(startDate, maturity, frequency,
      /* stub short */ true, /* from end */ true, businessDay, calendar, /* EOM */ false);
    
    final ZonedDateTime maturityWithOffset = protectStart ? maturity.plusDays(1) : maturity;
    
    CDSCouponDefinition[] coupons = new CDSCouponDefinition[paymentDates.length];
    final int maturityIndex = coupons.length - 1;
    
    if (maturityIndex > 0) {
    
      coupons[0] = new CDSCouponDefinition(currency, paymentDates[0], startDate, paymentDates[0],
        dayCount.getDayCountFraction(startDate, paymentDates[0]), notional, fixedRate);
      
      for (int i = 1; i < maturityIndex; i++) {
        coupons[i] = new CDSCouponDefinition(currency, paymentDates[i], paymentDates[i - 1], paymentDates[i],
          dayCount.getDayCountFraction(paymentDates[i - 1], paymentDates[i]), notional, fixedRate);
      }
      
      // TODO: extra time for protect start is included here, is this correct?
      coupons[maturityIndex] =  new CDSCouponDefinition(currency, paymentDates[maturityIndex], paymentDates[maturityIndex - 1], maturity,
        dayCount.getDayCountFraction(paymentDates[maturityIndex - 1], maturityWithOffset), notional, fixedRate);
      
    } else {
      
      // TODO: extra time for protect start is included here, is this correct?
      coupons[0] = new CDSCouponDefinition(currency, paymentDates[0], startDate, maturity,
        dayCount.getDayCountFraction(startDate, maturityWithOffset), notional, fixedRate);
    }
    
    return new CDSPremiumDefinition(coupons);
  }
  
  @Override
  public ISDACDSPremium toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final List<ISDACDSCoupon> resultList = new ArrayList<ISDACDSCoupon>();
    for (int loopcoupon = 0; loopcoupon < getPayments().length; loopcoupon++) {
      if (!date.isAfter(getNthPayment(loopcoupon).getPaymentDate())) {
        resultList.add(((CDSCouponDefinition) getNthPayment(loopcoupon)).toDerivative(date, yieldCurveNames));
      }
    }
    return new ISDACDSPremium(resultList.toArray(new ISDACDSCoupon[0]));
  }
}
