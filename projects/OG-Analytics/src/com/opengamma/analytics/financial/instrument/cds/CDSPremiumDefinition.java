/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.cds;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
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
  
  public CDSPremiumDefinition(CouponFixedDefinition[] payments) {
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
  public static CDSPremiumDefinition fromISDA(final Currency currency, final ZonedDateTime cdsStartDate, final ZonedDateTime cdsMaturityDate, final Frequency frequency,
    final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay, final double notional, final double fixedRate, final boolean protectStart) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(cdsStartDate, "cds start date");
    ArgumentChecker.notNull(cdsMaturityDate, "cds maturity date");
    ArgumentChecker.notNull(frequency, "frequency");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDay, "business day convention");
    ArgumentChecker.isTrue(!(dayCount instanceof ActualActualICMA) | !(dayCount instanceof ActualActualICMANormal), "Coupon per year required for Actua lActual ICMA");
    
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(cdsStartDate, cdsMaturityDate, frequency,
      /* stub short */ true, /* from end */ true, businessDay, calendar, /* EOM */ false);
    
    final ZonedDateTime maturityDate = protectStart ? cdsMaturityDate.plusDays(1) : cdsMaturityDate;
    
    CouponFixedDefinition[] coupons = new CouponFixedDefinition[paymentDates.length];
    final int maturityIndex = coupons.length - 1;
    
    if (maturityIndex > 0) {
    
      coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], cdsStartDate, paymentDates[0],
        dayCount.getDayCountFraction(cdsStartDate, paymentDates[0]), notional, fixedRate);
      
      for (int i = 1; i < maturityIndex; i++) {
        coupons[i] = new CouponFixedDefinition(currency, paymentDates[i], paymentDates[i - 1], paymentDates[i],
          dayCount.getDayCountFraction(paymentDates[i - 1], paymentDates[i]), notional, fixedRate);
      }
      
      coupons[maturityIndex] =  new CouponFixedDefinition(currency, paymentDates[maturityIndex], paymentDates[maturityIndex - 1], maturityDate,
        dayCount.getDayCountFraction(paymentDates[maturityIndex - 1], maturityDate), notional, fixedRate);
      
    } else {
      
      coupons[0] = new CouponFixedDefinition(currency, paymentDates[0], cdsStartDate, maturityDate,
        dayCount.getDayCountFraction(cdsStartDate, maturityDate), notional, fixedRate);
    }
    
    return new CDSPremiumDefinition(coupons);
  }
}
