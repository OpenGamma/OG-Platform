/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.calibration.CalibrateHazardRateTermStructureISDAMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda.ISDACompliantPremiumLegCalculator;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapIntegrationSchedule;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Class containing the methods for valuing a CDS which are common to all types of CDS 
 * e.g. the contingent leg calculation
 */
public class PresentValueCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final int cashSettlementDays = 3;

  // private static final boolean businessDayAdjustCashSettlementDate = true;
  // private static final BusinessDayConvention cashSettlementDateBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  // Create objects used for the construction of the various legs in the CDS valuation
  private static final GenerateCreditDefaultSwapPremiumLegSchedule premiumLegScheduleBuilder = new GenerateCreditDefaultSwapPremiumLegSchedule();
  private static final GenerateCreditDefaultSwapIntegrationSchedule contingentLegScheduleBuilder = new GenerateCreditDefaultSwapIntegrationSchedule();
  private static final GenerateCreditDefaultSwapIntegrationSchedule accruedLegScheduleBuilder = new GenerateCreditDefaultSwapIntegrationSchedule();

  // Create an object for calibrating a SNCDS
  private static final CalibrateHazardRateTermStructureISDAMethod cdsCalibrator = new CalibrateHazardRateTermStructureISDAMethod();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress
  // TODO : The code in this class is a complete mess at the moment - will be completely rewritten and modularised

  // TODO : Will need to revisit to look at how we can optimise the calculations (e.g. not repeating schedule generation calculations)
  // TODO : Need to add the PROT_PAY_MAT option as well

  // TODO : when the ISDA calibration routine fails, then should fall back to the simple bi-section that was originally implemented
  // TODO : since this routine very rarely falls over

  // TODO : Need to move the cashSettlemtndays variable and calculation methods into the standard CDS contract class
  // TODO : Review the use of day-count conventions in all the calculations (make sure they are all correct and consistent)
  // TODO : Need to move the calibrateAndGetPresentValue function?
  // TODO : Check if valDate > matDate and return zero if so in the contingent leg calculation
  // TODO : Remember that the start date for protection to begin (in the contingent leg calculation) is MAX(stepinDate, startDate)
  // TODO : Hook up the boolean for determining if we bda the cash settlement date and the adjustment convention

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS (replicates the calculation in the ISDA model)

  /**
   * Get the RPV01 of the premium leg  - i.e. the value of the leg per point of spread (expressed as a fraction, so 1bs is 0.0001) 
   * @param valuationDate The date that all cash-flows are PVed back to
   * @param cds Description of the CDS 
   * @param yieldCurve The discount curve
   * @param hazardRateCurve The survival curve 
   * @param priceType Clean or dirty 
   * @return The RPV01 of the premium leg
   * @deprecated Use ISDACompliantPremiumLegCalculator.calculateLeg
   */
  @Deprecated
  public double calculatePremiumLeg(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve, final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double presentValuePremiumLeg = 0.0;

    double thisPV = 0.0; // this sums the (risk discounted) premium payments ignoring the premium accrued on default (name from ISDA c code)

    int obsOffset = 0;

    final ZonedDateTime today = valuationDate;
    final ZonedDateTime stepinDate = cds.getEffectiveDate();

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Build the premium leg cashflow schedule from the contract specification
    final ZonedDateTime[][] premiumLegSchedule = premiumLegScheduleBuilder.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);

    // Build the integration schedule for the calculation of the accrued leg (this is not a cashflow schedule per se, but a set of time nodes for evaluating the accrued payment integral)
    final ZonedDateTime[] accruedLegIntegrationSchedule = accruedLegScheduleBuilder.constructCreditDefaultSwapAccruedLegIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, false);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Add the extra logic for this calculation (safe for the moment since 'protectionStart' is TRUE always)
    // TODO : ISDA uses accEndDates - check this
    // final ZonedDateTime matDate = cds.getMaturityDate();
    final ZonedDateTime matDate = premiumLegSchedule[premiumLegSchedule.length - 1][2].minusDays(1); // this is the final accrual end date minus one day

    // TODO : Check valueDate >= today and stepinDate >= today

    if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {

      // Trade has no remaining value
      presentValuePremiumLeg = 0.0;

      return presentValuePremiumLeg;
    }

    // The survival curve is defined as to the end of day. If the observation is at the start of day we subtract one.
    if (cds.getProtectionStart()) {
      obsOffset = -1;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Note the start index of this loop
    for (int i = 1; i < premiumLegSchedule.length; i++) {

      // ----------------------------------------------------------------------------------------------------------------------------------------

      final ZonedDateTime accrualStartDate = premiumLegSchedule[i][1];
      final ZonedDateTime accrualEndDate = premiumLegSchedule[i][2];
      final ZonedDateTime payDate = premiumLegSchedule[i][3];

      // If the stepinDate (valuationDate + 1) is after the end of the current accrual date, then this cashflow has already been realised
      if (!accrualEndDate.isAfter(stepinDate)) {
        continue;
      }

      // Calculate the time between the current accrual period start and end dates
      final double accTime = TimeCalculator.getTimeBetween(accrualStartDate, accrualEndDate, ACT_360);

      // Calculate the time between the current valuationDate and when the cashflow is paid (for purposes of discounting the cashflow)
      double t = TimeCalculator.getTimeBetween(today, payDate, ACT_365);

      // Calculate the time between the current valuation date and the end of the current (offset) accrual period
      double tObsOffset = TimeCalculator.getTimeBetween(today, accrualEndDate.plusDays(obsOffset), ACT_365);

      // TODO : Add this check for t
      // Compensate Java shortcoming
      if (Double.compare(tObsOffset, -0.0) == 0) {
        tObsOffset = 0;
      }
      // Calculate the survival probability and discount factor
      final double survival = hazardRateCurve.getSurvivalProbability(tObsOffset);
      final double discount = yieldCurve.getDiscountFactor(t);

      // Compute the contribution of this accrual period to the overall fee leg value
      thisPV += accTime * discount * survival;

      // ----------------------------------------------------------------------------------------------------------------------------------------

      double myPV = 0.0;

      // TODO : Extract out this calc into a separate routine
      // Do we want to include the accrued premium corresponding to this accrual period
      if (cds.getIncludeAccruedPremium()) {

        final ZonedDateTime offsetStepinDate = stepinDate.plusDays(obsOffset);
        final ZonedDateTime offsetAccStartDate = accrualStartDate.plusDays(obsOffset);
        final ZonedDateTime offsetAccEndDate = accrualEndDate.plusDays(obsOffset);

        // TODO : Check endDate > startDate

        final ZonedDateTime[] truncatedDateList = accruedLegScheduleBuilder.getTruncatedTimeLineDeprecated(accruedLegIntegrationSchedule, offsetAccStartDate, offsetAccEndDate);

        ZonedDateTime subStartDate;

        if (offsetStepinDate.isAfter(offsetAccStartDate)) {
          subStartDate = offsetStepinDate;
        } else {
          subStartDate = offsetAccStartDate;
        }

        final double tAcc = TimeCalculator.getTimeBetween(offsetAccStartDate, offsetAccEndDate, ACT_365);

        final double accRate = accTime / tAcc;

        t = TimeCalculator.getTimeBetween(today, subStartDate, ACT_365);

        // Compensate Java shortcoming
        if (Double.compare(t, -0.0) == 0) {
          t = 0;
        }
        double s0 = hazardRateCurve.getSurvivalProbability(t);
        double df0 = yieldCurve.getDiscountFactor(t);

        for (int j = 1; j < truncatedDateList.length; ++j) {

          double thisAccPV = 0.0;

          // Check this
          if (!truncatedDateList[j].isAfter(offsetStepinDate)) {

          } else {

            t = TimeCalculator.getTimeBetween(today, truncatedDateList[j], ACT_365);

            final double s1 = hazardRateCurve.getSurvivalProbability(t);
            final double df1 = yieldCurve.getDiscountFactor(t);

            final double t0 = TimeCalculator.getTimeBetween(offsetAccStartDate, subStartDate, ACT_365) + 0.5 / 365.0;
            final double t1 = TimeCalculator.getTimeBetween(offsetAccStartDate, truncatedDateList[j], ACT_365) + 0.5 / 365.0;
            t = t1 - t0;
            final double lambda = Math.log(s0 / s1) / t;
            final double fwdRate = Math.log(df0 / df1) / t;
            final double lambdafwdRate = lambda + fwdRate + 1.0e-50; // Note the hack here

            thisAccPV = lambda * accRate * s0 * df0 * ((t0 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) - (t1 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) * s1 / s0 * df1 / df0);
            myPV += thisAccPV;
            s0 = s1;
            df0 = df1;
            subStartDate = truncatedDateList[j];
          }
        }
      } // end if acc fee payment

      // ----------------------------------------------------------------------------------------------------------------------------------------

      thisPV += myPV;

    } // end loop over fee leg payments

    presentValuePremiumLeg = thisPV;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double valueDatePV = calculateCashSettlementDiscountFactor(cds, valuationDate, cashSettlementDays, /* cashSettlementDateBusinessDayConvention, */yieldCurve);

    presentValuePremiumLeg /= valueDatePV;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Do we want to calculate the clean price (includes the previously accrued portion of the premium)
    if (priceType == PriceType.CLEAN) {
      presentValuePremiumLeg -= calculateAccruedInterest(cds, premiumLegSchedule, stepinDate);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return cds.getNotional() * presentValuePremiumLeg;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the contribution to the premium leg value from coupon accruals between cashflow dates

  private double calculatePremiumLegAccrued() {

    double ai = 0.0;

    return ai;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the accrued interest between the last accrual date and the current valuation date

  public double calculateAccruedInterest(final CreditDefaultSwapDefinition cds, final ZonedDateTime[][] premiumLegSchedule, final ZonedDateTime stepinDate) {

    // TODO : Maybe check if stepinDate is in range [startDate, maturityDate + 1] - probably not necessary since the valuation will not allow this
    // TODO : Would be useful to re-write this to make it clearer

    // Start at the beginning of the cashflow schedule
    // ZonedDateTime rollingDate = premiumLegSchedule[0];
    ZonedDateTime rollingDate = premiumLegSchedule[0][0];
    // ZonedDateTime rollingDate = premiumLegSchedule[1][1];

    // Check if the stepin date falls on any of the accrual begin dates
    for (int i = 1; i < premiumLegSchedule.length; i++) {
      if (stepinDate.equals(premiumLegSchedule[i][1])) {
        // If it does, then there has been no accrued interest
        return 0.0;
      }
    }

    int startCashflowIndex = 0;
    // int startCashflowIndex = 1;

    // step through the cashflow schedule until we get to the step in date
    while (rollingDate.isBefore(stepinDate)) {
      startCashflowIndex++;
      // rollingDate = premiumLegSchedule[startCashflowIndex];
      rollingDate = premiumLegSchedule[startCashflowIndex][0];
      // rollingDate = premiumLegSchedule[startCashflowIndex][1];
    }

    // Get the date of the last coupon before the current valuation date
    // final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1];
    final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1][0];
    // final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1][1];

    final double ai = cds.getDayCountFractionConvention().getDayCountFraction(previousPeriod, stepinDate);

    return ai;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to compute the discount factor for discounting the upfront payment made at the cash settlement date back to the valuation date

  private double calculateCashSettlementDiscountFactor(final CreditDefaultSwapDefinition cds, final ZonedDateTime spotDate, final int spotDays,
  /* final BusinessDayConvention cashSettlementDateBusinessDayConvention, */
  final ISDADateCurve yieldCurve) {

    // From the spotDate, determine the next working day spotDays in the future
    ZonedDateTime bdaCashSettlementDate = ScheduleUtils.calculateWorkday(cds, spotDate, spotDays);

    // Compute the time between the spotDate and the business day adjusted cash settlement date
    final double timeToCashSettlement = TimeCalculator.getTimeBetween(spotDate, bdaCashSettlementDate);

    // Compute the discount factor
    final double cashSettlementDateDiscountFactor = yieldCurve.getDiscountFactor(timeToCashSettlement);

    return cashSettlementDateDiscountFactor;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the contingent leg (replicates the calculation in the ISDA model)

  @Deprecated
  public double calculateContingentLeg(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve, final HazardRateCurve hazardRateCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Local variable definitions
    double presentValueContingentLeg = 0.0;

    int offset = 0;

    if (cds.getProtectionStart()) {
      offset = 1;
    }

    ZonedDateTime startDate;
    ZonedDateTime clStartDate = valuationDate;

    final ZonedDateTime clEndDate = cds.getMaturityDate();
    final ZonedDateTime stepinDate = cds.getEffectiveDate();

    // TODO : Review the following if statements

    if (cds.getProtectionStart()) {
      clStartDate = valuationDate.minusDays(1);
    }

    if (clStartDate.isAfter(stepinDate.minusDays(offset))) {
      startDate = clStartDate;
    } else {
      startDate = stepinDate.minusDays(offset);
    }

    if (startDate.isAfter(valuationDate.minusDays(1))) {
      // startDate = startDate;
    } else {
      startDate = valuationDate.minusDays(1);
    }

    if (valuationDate.isAfter(clEndDate)) {

      presentValueContingentLeg = 0.0;

      return presentValueContingentLeg;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Build the integration schedule for the calculation of the contingent leg (this is not a cashflow schedule per se, but a set of timenodes for evaluating the contingent leg integral)
    final double[] contingentLegIntegrationSchedule = contingentLegScheduleBuilder.constructCreditDefaultSwapContingentLegIntegrationSchedule(valuationDate, startDate, clEndDate, cds, yieldCurve,
        hazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Get the survival probability at the first point in the integration schedule
    double survivalProbability = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[0]);

    // Get the discount factor at the first point in the integration schedule
    double discountFactor = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[0]);

    // Compute the loss given default
    final double lossGivenDefault = (1 - cds.getRecoveryRate());

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop over each of the points in the integration schedule
    for (int i = 1; i < contingentLegIntegrationSchedule.length; ++i) {

      // Calculate the time between adjacent points in the integration schedule (note this can be zero which causes problems later)
      final double deltat = contingentLegIntegrationSchedule[i] - contingentLegIntegrationSchedule[i - 1];

      // Set the probability of survival up to the previous point in the integration schedule
      final double survivalProbabilityPrevious = survivalProbability;

      // Set the discount factor up to the previous point in the integration schedule
      final double discountFactorPrevious = discountFactor;

      // Get the survival probability at this point in the integration schedule
      survivalProbability = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[i]);

      // Get the discount factor at this point in the integration schedule
      discountFactor = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[i]);

      // Calculate the forward hazard rate over the interval deltat (assumes the hazard rate is constant over this period)
      final double hazardRate = Math.log(survivalProbabilityPrevious / survivalProbability) / deltat;

      // Calculate the forward interest rate over the interval deltat (assumes the interest rate is constant over this period)
      final double interestRate = Math.log(discountFactorPrevious / discountFactor) / deltat;

      // Calculate the contribution of the interval deltat to the overall contingent leg integral (if deltat is v.small the log's above can return zero, therefore have to check for this)
      if (Double.compare(hazardRate, 0.0) != 0) {
        presentValueContingentLeg += lossGivenDefault * (hazardRate / (hazardRate + interestRate)) * (1.0 - Math.exp(-(hazardRate + interestRate) * deltat)) * survivalProbabilityPrevious
            * discountFactorPrevious;
      }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double valueDatePV = calculateCashSettlementDiscountFactor(cds, valuationDate, cashSettlementDays, /* cashSettlementDateBusinessDayConvention, */yieldCurve);

    return cds.getNotional() * presentValueContingentLeg / valueDatePV;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calibrate a CDS to supplied market data, and then calculate the PV

  @Deprecated
  public double calibrateAndGetPresentValue(final ZonedDateTime valuationDate, final LegacyVanillaCreditDefaultSwapDefinition cds, final ZonedDateTime[] marketTenors, final double[] marketSpreads,
      final ISDADateCurve yieldCurve, final PriceType priceType) {

    // Create a CDS for valuation
    final LegacyVanillaCreditDefaultSwapDefinition valuationCDS = cds;

    // Call the constructor to create a CDS present value object
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Build a hazard rate curve object based on the input market data
    final HazardRateCurve calibratedHazardRateCurve = cdsCalibrator.isdaCalibrateHazardRateCurve(valuationDate, valuationCDS, marketTenors, marketSpreads, yieldCurve);

    // Calculate the CDS PV using the just calibrated hazard rate term structure
    final double presentValue = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, valuationCDS, yieldCurve, calibratedHazardRateCurve, priceType);

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
