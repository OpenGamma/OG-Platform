/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

/**
 *  Class containing methods for the valuation of a legacy vanilla CDS 
 */
public class PresentValueCreditDefaultSwap {

  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds, double [][]cashflowSchedule) {

    // -------------------------------------------------------------------------------------------------
    
    // Hardcode the number of cashflows - will change this when implement the schedule generator
    int n = 20;
    
    // Array to hold the cashflow schedule
    double dummyCashflowSchedule[][] = new double [n + 1][4];
    
    // Generate the schedule of cashflows for the premium leg
    generateCDSPremiumLegSchedule(cds);
    
    // Calculate the value of the premium leg
    double presentValuePremiumLeg = calculatePremiumLeg(cds, cashflowSchedule);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds);
    
    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -presentValuePremiumLeg + presentValueContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection().equalsIgnoreCase("Sell")) {
      presentValue = -1 * presentValue;
    }
    
    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------

  // TODO : Seperate out the accrued premium calc out into another method (so users can see contribution of this directly)
  // TODO : Add a method to calc both the legs in one go (is this useful or not?)

  // Method to calculate the value of the fee leg

  // We assume the schedule of coupon payment dates represented as doubles (suitably generated) has been computed externally and is passed in via cashflowSchedule
  // We assume the discount factors have been computed externally and passed in with the CDS object
  // We assume the 'calibrated' survival probabilities have been computed externally and are passed in with the CDS object

  // Will replace these three dummy 'objects' with suitably computed objects in due course

  // For now, assuming we are on a cashflow date (for testing purposes) - will need to add the correction for a seasoned trade

  double calculatePremiumLeg(CreditDefaultSwapDefinition cds, double [][]cashflowSchedule) {

    double presentValuePremiumLeg = 0.0;
    double presentValueAccruedPremium = 0.0;

    // Determine how many premium cashflows there are in the original contract schedule (this includes time zero even though there is no cashfow on this date
    int n = cashflowSchedule.length;

    // Get the notional amount to multiply the premium leg by
    double notional = cds.getNotional();

    // Get the CDS par spread (remember this is supplied in bps, therefore needs to be divided by 10,000)
    double parSpread = cds.getParSpread() / 10000.0;

    // get the yield curve
    YieldCurve yieldCurve = cds.getYieldCurve();

    // Get the survival curve
    YieldCurve survivalCurve = cds.getSurvivalCurve();

    // Do we need to calculate the accrued premium as well
    boolean includeAccruedPremium = cds.getIncludeAccruedPremium();

    // Loop through all the elements (times and dcf's) in the cashflow schedule (note limits of loop)
    for (int i = 1; i < n; i++) {

      double t = cashflowSchedule[i][0];
      double dcf = cashflowSchedule[i][1];

      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = survivalCurve.getDiscountFactor(t);

      presentValuePremiumLeg += dcf * discountFactor * survivalProbability;

      // If required, calculate the accrued premium contribution to the overall premium leg
      if (includeAccruedPremium) {

        double tPrevious = cashflowSchedule[i - 1][0];
        double survivalProbabilityPrevious = survivalCurve.getDiscountFactor(tPrevious);

        presentValueAccruedPremium += 0.5 * dcf * discountFactor * (survivalProbabilityPrevious - survivalProbability);
      }
    }

    return parSpread * notional * (presentValuePremiumLeg + presentValueAccruedPremium);
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the value of the contingent leg

  double calculateContingentLeg(CreditDefaultSwapDefinition cds) {

    double presentValueContingentLeg = 0.0;

    // Get the notional amount to multiply the contingent leg by
    double notional = cds.getNotional();

    // get the yield curve
    YieldCurve yieldCurve = cds.getYieldCurve();

    // Get the survival curve
    YieldCurve survivalCurve = cds.getSurvivalCurve();

    // Hardcoded hack for now - will remove when work out how to use ZonedDateTime
    int tVal = 0;
    int maturity = 5;
    
    ZonedDateTime maturityDate = cds.getMaturityDate();
    ZonedDateTime valuationDate = cds.getValuationDate();
    
    int numDays = DateUtils.getDaysBetween(valuationDate, maturityDate);

    int numberOfIntegrationSteps = cds.getNumberOfIntegrationSteps();

    // Check this calculation more carefully - is the proper way to do it
    //int numberOfPartitions = (int) (numberOfIntegrationSteps * numDays / 365.25 + 0.5); 
    
    int numberOfPartitions = (int) (numberOfIntegrationSteps * (maturity - tVal) + 0.5);

    double epsilon = (double) (maturity - tVal) / (double) numberOfPartitions;

    double valuationRecoveryRate = cds.getValuationRecoveryRate();

    for (int k = 1; k < numberOfPartitions; k++) {

      double t = k * epsilon;
      double tPrevious = (k - 1) * epsilon;

      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = survivalCurve.getDiscountFactor(t);
      double survivalProbabilityPrevious = survivalCurve.getDiscountFactor(tPrevious);

      presentValueContingentLeg += discountFactor * (survivalProbabilityPrevious - survivalProbability);
    }

    return notional * (1 - valuationRecoveryRate) * presentValueContingentLeg;
  }
  
//-------------------------------------------------------------------------------------------------
  
  // Method to generate the schedule of CDS premium leg cashflows given the user specified contract parameters
  
  // Will re-write this once functionality is implemented
  
  void generateCDSPremiumLegSchedule(CreditDefaultSwapDefinition cds) {
    
    ZonedDateTime startDate = cds.getStartDate();
    ZonedDateTime maturityDate = cds.getMaturityDate();
    
    //boolean adjustMaturityDate = cds.getAdjustMaturityDate();
    
    Calendar calendar = cds.getCalendar();
    
    if (calendar.equals(null)) {
      // No calender, therefore don't adjust any dates for non-business days
    }
    
    ZonedDateTime adjustedEffectiveDate = adjustEffectiveDate(startDate, calendar);
    ZonedDateTime immAdjustedMaturityDate = immAdjustMaturityDate(maturityDate, calendar);
    
    
    /*
    int numberOfCashflows = 1;
    
    while (maturityDate.isAfter(effectiveDate)) {
      maturityDate = maturityDate.minusMonths(3);
      numberOfCashflows++;
      
      System.out.println("Cashflow on " + maturityDate + ", " + numberOfCashflows + " cashflows");
    }
    
    //int year = DateUtils.getDaysBetween(effectiveDate, maturityDate);
    //System.out.println(year);
    
    System.out.println("Adjusted Maturity Date = " + maturityDate);
    */
    
    System.out.println("Start Date = " + startDate);
    System.out.println("Adjusted Effective Date = " + adjustedEffectiveDate);
    System.out.println("Unadjusted Maturity Date = " + maturityDate);
    System.out.println("IMM Adjusted Maturity Date = " + immAdjustedMaturityDate);
  }
  
//-------------------------------------------------------------------------------------------------
  
  public ZonedDateTime adjustEffectiveDate(ZonedDateTime startDate, Calendar calendar) {
    
    // Set the effective date to be T + 1
    ZonedDateTime effectiveDate = startDate.plusDays(1);
    
    // Adjust the effective date so it does not fall on a non-business day
    while (!calendar.isWorkingDay(effectiveDate.toLocalDate())) {
      effectiveDate = effectiveDate.plusDays(1);
    }
    
    return effectiveDate;
  }
  
//-------------------------------------------------------------------------------------------------

  public ZonedDateTime immAdjustMaturityDate(ZonedDateTime maturityDate, Calendar calendar) {
    
    ZonedDateTime immAdjustedMaturityDate = maturityDate;
    
    final int day = maturityDate.getDayOfMonth();
    final int month = maturityDate.getMonthOfYear().getValue();
    final int year = maturityDate.getYear();
    
    final ZonedDateTime immDatePreviousDecember = DateUtils.getUTCDate(year - 1, 12, 20);
    final ZonedDateTime immDateMarch = DateUtils.getUTCDate(year, 3, 20);
    final ZonedDateTime immDateJune = DateUtils.getUTCDate(year, 6, 20);
    final ZonedDateTime immDateSeptember = DateUtils.getUTCDate(year, 9, 20);
    final ZonedDateTime immDateDecember = DateUtils.getUTCDate(year, 12, 20);
    final ZonedDateTime immDateNextMarch = DateUtils.getUTCDate(year + 1, 3, 20);
    
    /*
    System.out.println(immDatePreviousDecember);
    System.out.println(immDateMarch);
    System.out.println(immDateJune);
    System.out.println(immDateSeptember);
    System.out.println(immDateDecember);
    System.out.println(immDateNextMarch);
    */
    
    // -------------------------------------------------------------------
    
    // First of all check that the maturity date isn't one of the IMM dates for 'year'
    // These tests might be a bit redundant
    
    // Is the maturity date equal to the March IMM date of 'year'
    if (maturityDate.equals(immDateMarch)) {
      immAdjustedMaturityDate = immDateMarch;
    }
    
    // Is the maturity date equal to the June IMM date of 'year'
    if (maturityDate.equals(immDateJune)) {
      immAdjustedMaturityDate = immDateJune;
    }
    
    // Is the maturity date equal to the Seotember IMM date of 'year'
    if (maturityDate.equals(immDateSeptember)) {
      immAdjustedMaturityDate = immDateSeptember;
    }
    
    // Is the maturity date equal to the December IMM date of 'year'
    if (maturityDate.equals(immDateDecember)) {
      immAdjustedMaturityDate = immDateDecember;
    }
    
    // -------------------------------------------------------------------
    
    if (maturityDate.isAfter(immDatePreviousDecember) && maturityDate.isBefore(immDateMarch)) {
      immAdjustedMaturityDate = immDateMarch;
    }
    
    if (maturityDate.isAfter(immDateMarch) && maturityDate.isBefore(immDateJune)) {
      immAdjustedMaturityDate = immDateJune;
    }
    
    if (maturityDate.isAfter(immDateJune) && maturityDate.isBefore(immDateSeptember)) {
      immAdjustedMaturityDate = immDateSeptember;
    }
    
    if (maturityDate.isAfter(immDateSeptember) && maturityDate.isBefore(immDateDecember)) {
      immAdjustedMaturityDate = immDateDecember;
    }
    
    if (maturityDate.isAfter(immDateDecember) && maturityDate.isBefore(immDateNextMarch)) {
      immAdjustedMaturityDate = immDateNextMarch;
    }
    
    // -------------------------------------------------------------------
    
    //System.out.println("Day = " + day + ", month = " + month + ", year = " + year);
    
    return immAdjustedMaturityDate;
  }
  
//-------------------------------------------------------------------------------------------------
  
  public ZonedDateTime adjustMaturityDate(ZonedDateTime maturityDate, Calendar calendar, boolean adjustMaturityDate) {
    
    ZonedDateTime adjustedMaturityDate = maturityDate;
    
    // If required adjust the maturity date so it does not fall on a non-business day
    if (adjustMaturityDate) {
      while (!calendar.isWorkingDay(adjustedMaturityDate.toLocalDate())) {
        adjustedMaturityDate = adjustedMaturityDate.plusDays(1);
      }
    }
    
    return adjustedMaturityDate;
  }

  //-------------------------------------------------------------------------------------------------
}
