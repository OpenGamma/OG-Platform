/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.analytics.model.cds.date.Calendars;
import com.opengamma.financial.analytics.model.cds.date.DateFunctions;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.util.time.DateUtils;

/**
 * Do the actual CDS calculations for a single period
 * 
 * @author Niels Stchedroff
 */
public class CDSSimpleCalculator {

  public static double calculate(CDSSecurity cds, BondSecurity bond, YieldAndDiscountCurve bondCcyCurve, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve riskyCurve, ZonedDateTime pricingDate)
    throws OpenGammaRuntimeException {

    Calendars cdsCalendar = Calendars.EU;

    // TODO: Move to TenorUtils class and use Tenor object in CDSSecurity
    final Period cdsTenor = getTenor(cds.getPremiumFrequency());
    final double interestCashflow = cds.getPremiumRate() * (cdsTenor.totalMonths() / 12.0 + cdsTenor.totalSecondsWith24HourDays() / DateUtils.SECONDS_PER_YEAR);
    final double defaultPayoutCashflow = cds.getNotional() * (1.0 - cds.getRecoveryRate());
    
    final int numberOfCdsPremiumDates = DateFunctions.numberOfDates(cds.getMaturity(), getTenor(cds.getPremiumFrequency()), cdsCalendar, pricingDate);
    final int numberOfBondPremiumDates = DateFunctions.numberOfDates(bond.getLastTradeDate().getExpiry(), getTenor(bond.getCouponFrequency()), cdsCalendar, pricingDate);
    
    double total = 0.0;
    
    System.out.println("Premium leg");
    
    System.out.println("period \t cdsPremiumDate \t timeToCdsPremium \t bondCcyRate \t\t cdsCcyRate \t\t riskyRate \t\t "
      + "bondCcyDiscountFactor \t cdsCcyDiscountFactor \t riskyDiscountFactor \t "
      + "bondCcyDiscountFactor2 cdsCcyDiscountFactor2 \t riskyDiscountFactor2 "
      + "riskFreeDefaultProbability expectedCashflow \t discountedExpCashflow"
    );

    // Premium leg
    for (int i = 1; i <= numberOfCdsPremiumDates; ++i) {

      final ZonedDateTime cdsPremiumDate = cdsPremiumDate(i, numberOfCdsPremiumDates, cds.getMaturity(), getTenor(cds.getPremiumFrequency()));
      final double timeToCdsPremium = DateUtils.getDifferenceInYears(pricingDate, cdsPremiumDate);
      
      final double bondCcyRate = bondCcyCurve.getInterestRate(timeToCdsPremium);
      final double cdsCcyRate = cdsCcyCurve.getInterestRate(timeToCdsPremium);
      final double riskyRate = riskyCurve.getInterestRate(timeToCdsPremium);
      final double bondCcyDiscountFactor = 1.0 / Math.pow((1.0 + bondCcyRate), timeToCdsPremium);
      final double cdsCcyDiscountFactor = 1.0 / Math.pow((1.0 + cdsCcyRate), timeToCdsPremium);
      final double riskyDiscountFactor = 1.0 / Math.pow((1.0 + riskyRate), timeToCdsPremium);
      
      final double bondCcyDiscountFactor2 = bondCcyCurve.getDiscountFactor(timeToCdsPremium);
      final double cdsCcyDiscountFactor2 = cdsCcyCurve.getDiscountFactor(timeToCdsPremium);
      final double riskyDiscountFactor2 = riskyCurve.getDiscountFactor(timeToCdsPremium);
            
      final double riskFreeDefaultProbability = (1.0 - (riskyDiscountFactor / bondCcyDiscountFactor)) / (1.0 - cds.getRecoveryRate());
      
      final double expectedCashflow = interestCashflow * (1.0 - riskFreeDefaultProbability);
      final double discountedExpCashflow = expectedCashflow * cdsCcyDiscountFactor;
      total += discountedExpCashflow;
      
      System.out.println(i + "\t" + cdsPremiumDate.toString() + "\t" + timeToCdsPremium + "\t"
        + bondCcyRate + "\t" + cdsCcyRate + "\t" + riskyRate + "\t"
        + bondCcyDiscountFactor + "\t" + cdsCcyDiscountFactor + "\t" + riskyDiscountFactor + "\t"
        + bondCcyDiscountFactor2 + "\t" + cdsCcyDiscountFactor2 + "\t" + riskyDiscountFactor2 + "\t"
        + riskFreeDefaultProbability + "\t" + expectedCashflow + "\t" + discountedExpCashflow
      );
    }
    
    System.out.println("Default leg");
    
    // Default leg
    final int defaultLegPeriods = cds.getMaturity().isAfter(bond.getLastTradeDate().getExpiry()) 
      ? numberOfBondPremiumDates + 1
      : numberOfBondPremiumDates;
    
    double previousRiskFreeDefaultProbability = 0.0;
    
    for (int i = 1; i <= defaultLegPeriods; ++i) {
      
      final ZonedDateTime bondPremiumDate = i <= numberOfBondPremiumDates
        ? bondPremiumDate(i, numberOfBondPremiumDates, bond.getLastTradeDate().getExpiry(), getTenor(bond.getCouponFrequency()))
        : cds.getMaturity();
      final double timeToBondPremium = DateUtils.getDifferenceInYears(pricingDate, bondPremiumDate);
      
      final double bondCcyRate = bondCcyCurve.getInterestRate(timeToBondPremium);
      final double cdsCcyRate = cdsCcyCurve.getInterestRate(timeToBondPremium);
      final double riskyRate = riskyCurve.getInterestRate(timeToBondPremium);
      final double bondCcyDiscountFactor = 1.0 / Math.pow((1.0 + bondCcyRate), timeToBondPremium);
      final double cdsCcyDiscountFactor = 1.0 / Math.pow((1.0 + cdsCcyRate), timeToBondPremium);  
      final double riskyDiscountFactor = 1.0 / Math.pow((1.0 + riskyRate), timeToBondPremium);
      
      final double bondCcyDiscountFactor2 = bondCcyCurve.getDiscountFactor(timeToBondPremium);
      final double cdsCcyDiscountFactor2 = cdsCcyCurve.getDiscountFactor(timeToBondPremium);
      final double riskyDiscountFactor2 = riskyCurve.getDiscountFactor(timeToBondPremium);
       
      final double riskFreeDefaultProbability = (1.0 - (riskyDiscountFactor / bondCcyDiscountFactor)) / (1.0 - cds.getRecoveryRate());
      
      final double expectedDefaultPayout = defaultPayoutCashflow * (riskFreeDefaultProbability - previousRiskFreeDefaultProbability);
      final double discountedExpDefaultPayout = expectedDefaultPayout * cdsCcyDiscountFactor;
      total -= discountedExpDefaultPayout;

      previousRiskFreeDefaultProbability = riskFreeDefaultProbability;
      
      System.out.println(i + "\t" + bondPremiumDate.toString() + "\t" + timeToBondPremium + "\t"
        + bondCcyRate + "\t" + cdsCcyRate + "\t" + riskyRate + "\t"
        + bondCcyDiscountFactor + "\t" + cdsCcyDiscountFactor + "\t" + riskyDiscountFactor + "\t"
        + bondCcyDiscountFactor2 + "\t" + cdsCcyDiscountFactor2 + "\t" + riskyDiscountFactor2 + "\t"
        + riskFreeDefaultProbability + "\t" + expectedDefaultPayout + "\t" + discountedExpDefaultPayout
      );
    }

    return total;
  }

  private static ZonedDateTime cdsPremiumDate(int period, int numberOfCdsPremiumDates, ZonedDateTime cdsMaturity, Period cdsTerm) throws OpenGammaRuntimeException {
      
    BusinessDayConvention convention = new FollowingBusinessDayConvention();
    Calendar calendar = new MondayToFridayCalendar("A");
    
    return convention.adjustDate(calendar, cdsMaturity.minus(cdsTerm.multipliedBy(numberOfCdsPremiumDates - period)));
  }

  private static ZonedDateTime bondPremiumDate(int period, int numberOfBondPremiumDates, ZonedDateTime bondMaturity, Period bondTerm) {

    BusinessDayConvention convention = new FollowingBusinessDayConvention();
    Calendar calendar = new MondayToFridayCalendar("A");

    return convention.adjustDate(calendar, bondMaturity.minus(bondTerm.multipliedBy(numberOfBondPremiumDates - period)));
  }

  // TODO: Should this method be available somewhere central?
  // TODO: Why do other security types have conversion visitors to translate to security definition classes?
  private static Period getTenor(final Frequency freq) {
    if (freq instanceof PeriodFrequency) {
      return ((PeriodFrequency) freq).getPeriod();
    } else if (freq instanceof SimpleFrequency) {
      return ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
    }
    throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
  }

}
