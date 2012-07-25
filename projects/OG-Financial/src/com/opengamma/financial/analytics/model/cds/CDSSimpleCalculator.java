/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.cds;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
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

  private static final Logger s_logger = LoggerFactory.getLogger(CDSSimpleCalculator.class);

  public static double calculate(CDSSecurity cds, BondSecurity bond, YieldAndDiscountCurve bondCcyCurve, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve riskyCurve, ZonedDateTime pricingDate)
    throws OpenGammaRuntimeException {

    BusinessDayConvention convention = new FollowingBusinessDayConvention();
    Calendar calendar = new MondayToFridayCalendar("A");

    return calculatePremiumLeg(cds, bond, bondCcyCurve, cdsCcyCurve, riskyCurve, pricingDate, calendar, convention)
      - calculateDefaultLeg(cds, bond, bondCcyCurve, cdsCcyCurve, riskyCurve, pricingDate, calendar, convention);
  }

  private static double calculatePremiumLeg(CDSSecurity cds, BondSecurity bond, YieldAndDiscountCurve bondCcyCurve, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve riskyCurve,
    ZonedDateTime pricingDate, Calendar calendar, BusinessDayConvention convention)
    throws OpenGammaRuntimeException {

    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Premium leg");
      s_logger.debug("period, cdsPremiumDate, timeToCdsPremium, bondCcyRate, cdsCcyRate, riskyRate, "
        + "bondCcyDiscountFactor, cdsCcyDiscountFactor, riskyDiscountFactor, "
        + "bondCcyDiscountFactor2, cdsCcyDiscountFactor2, riskyDiscountFactor2, "
        + "riskFreeDefaultProbability, expectedCashflow, discountedExpCashflow");
    }

    // TODO: Move to TenorUtils class and use Tenor object in CDSSecurity
    final ZonedDateTime cdsMaturity = cds.getMaturity();
    final Period cdsTenor = getTenor(cds.getPremiumFrequency());

    final double interestCashflow = cds.getPremiumRate() * (cdsTenor.totalMonths() / 12.0 + cdsTenor.totalSecondsWith24HourDays() / DateUtils.SECONDS_PER_YEAR);
    final int numberOfCdsPremiumDates = numberOfDates(pricingDate, cdsMaturity, cdsTenor, calendar, convention);

    System.out.println(numberOfCdsPremiumDates);

    double total = 0.0;

    for (int i = 1; i <= numberOfCdsPremiumDates; ++i) {

      final ZonedDateTime cdsPremiumDate = convention.adjustDate(calendar, cds.getMaturity().minus(cdsTenor.multipliedBy(numberOfCdsPremiumDates - i)));
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

      if (s_logger.isDebugEnabled()) {
        s_logger.debug(i + "," + cdsPremiumDate.toString() + "," + timeToCdsPremium + ","
          + bondCcyRate + "," + cdsCcyRate + "," + riskyRate + ","
          + bondCcyDiscountFactor + "," + cdsCcyDiscountFactor + "," + riskyDiscountFactor + ","
          + bondCcyDiscountFactor2 + "," + cdsCcyDiscountFactor2 + "," + riskyDiscountFactor2 + ","
          + riskFreeDefaultProbability + "," + expectedCashflow + "," + discountedExpCashflow);
      }
    }

    return total;
  }

  private static double calculateDefaultLeg(CDSSecurity cds, BondSecurity bond, YieldAndDiscountCurve bondCcyCurve, YieldAndDiscountCurve cdsCcyCurve, YieldAndDiscountCurve riskyCurve,
    ZonedDateTime pricingDate, Calendar calendar, BusinessDayConvention convention)
    throws OpenGammaRuntimeException {

    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Default leg");
      s_logger.debug("period, bondPremiumDate, timeToBondPremium, bondCcyRate, cdsCcyRate, riskyRate, "
        + "bondCcyDiscountFactor, cdsCcyDiscountFactor, riskyDiscountFactor, "
        + "bondCcyDiscountFactor2, cdsCcyDiscountFactor2, riskyDiscountFactor2, "
        + "riskFreeDefaultProbability, expectedCashflow, discountedExpCashflow");
    }

    final ZonedDateTime cdsMaturity = cds.getMaturity();
    final ZonedDateTime bondMaturity = bond.getLastTradeDate().getExpiry();
    final Period bondTenor = getTenor(bond.getCouponFrequency());

    final double defaultPayoutCashflow = cds.getNotional() * (1.0 - cds.getRecoveryRate());
    final int numberOfBondPremiumDates = numberOfDates(pricingDate, bondMaturity, bondTenor, calendar, convention);
    final int defaultLegPeriods = cdsMaturity.isAfter(bondMaturity)
      ? numberOfBondPremiumDates + 1
      : numberOfBondPremiumDates;

    double total = 0.0;
    double previousRiskFreeDefaultProbability = 0.0;

    for (int i = 1; i <= defaultLegPeriods; ++i) {

      final ZonedDateTime bondPremiumDate = i <= numberOfBondPremiumDates
        ? convention.adjustDate(calendar, bondMaturity.minus(bondTenor.multipliedBy(numberOfBondPremiumDates - i)))
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
      total += discountedExpDefaultPayout;

      previousRiskFreeDefaultProbability = riskFreeDefaultProbability;

      if (s_logger.isDebugEnabled()) {
        s_logger.debug(i + ", " + bondPremiumDate.toString() + ", " + timeToBondPremium + ", "
          + bondCcyRate + ", " + cdsCcyRate + ", " + riskyRate + ", "
          + bondCcyDiscountFactor + ", " + cdsCcyDiscountFactor + ", " + riskyDiscountFactor + ", "
          + bondCcyDiscountFactor2 + ", " + cdsCcyDiscountFactor2 + ", " + riskyDiscountFactor2 + ", "
          + riskFreeDefaultProbability + ", " + expectedDefaultPayout + ", " + discountedExpDefaultPayout);
      }
    }

    return total;
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

  // TODO: Should this be a central method somewhere like e.g. DateUtils
  public static int numberOfDates(ZonedDateTime pricingDate, ZonedDateTime maturity, Period term, Calendar calendar, BusinessDayConvention convention)
  {
    ZonedDateTime paymentDate = maturity;
    int periods = 0;

    while (paymentDate.isAfter(pricingDate)) {
      paymentDate = convention.adjustDate(calendar, maturity.minus(term.multipliedBy(++periods)));
    }

    return periods;
  }

}
