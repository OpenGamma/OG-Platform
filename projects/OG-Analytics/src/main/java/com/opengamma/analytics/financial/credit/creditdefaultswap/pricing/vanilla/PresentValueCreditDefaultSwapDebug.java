/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantScheduleGenerator.toLocalDate;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantScheduleGenerator.getIntegrationNodesAsDates;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantScheduleGenerator.truncateList;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.calibration.CalibrateHazardRateTermStructureISDAMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantScheduleGenerator;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDAPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleUtils;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * This is a version  of PresentValueCreditDefaultSwap for testing/debugging purposes only. <b>Do not use in any production code.</b>
 * Class containing the methods for valuing a CDS which are common to all types of CDS 
 * e.g. the contingent leg calculation
 */
public class PresentValueCreditDefaultSwapDebug {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final int cashSettlementDays = 3;

  // private static final boolean businessDayAdjustCashSettlementDate = true;
  // private static final BusinessDayConvention cashSettlementDateBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  // // Create objects used for the construction of the various legs in the CDS valuation
  // private static final GenerateCreditDefaultSwapPremiumLegSchedule premiumLegScheduleBuilder = new GenerateCreditDefaultSwapPremiumLegSchedule();
  // private static final GenerateCreditDefaultSwapIntegrationSchedule contingentLegScheduleBuilder = new GenerateCreditDefaultSwapIntegrationSchedule();
  // private static final GenerateCreditDefaultSwapIntegrationSchedule accruedLegScheduleBuilder = new GenerateCreditDefaultSwapIntegrationSchedule();

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
    ArgumentChecker.notNull(valuationDate, "null valuationDate");
    ArgumentChecker.notNull(cds, "null cds");
    ArgumentChecker.notNull(yieldCurve, "null yieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "null hazardRateCurve");
    ArgumentChecker.notNull(priceType, "null priceType");

    final LocalDate valueDate = valuationDate.toLocalDate();
    final LocalDate today = valueDate; // TODO in the ISDA code today is a separate input to value(ation)Date - here they are the same
    // TODO uncomment this once it is a usful test (i.e. donot set today = valueDate)
    // ArgumentChecker.isFalse(valueDate.isBefore(today), "require valueDate > = today");
    final LocalDate stepinDate = cds.getEffectiveDate().toLocalDate();
    ArgumentChecker.isFalse(stepinDate.isBefore(today), "require stepinDate >= today");

    double presentValuePremiumLeg = 0.0;
    double thisPV = 0.0; // this sums the (risk discounted) premium payments ignoring the premium accrued on default (name from ISDA c code)
    int obsOffset = 0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // // Build the premium leg cashflow schedule from the contract specification
    // final ZonedDateTime[][] premiumLegSchedule = premiumLegScheduleBuilder.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);

    final LocalDate startDate = cds.getStartDate().toLocalDate();
    final LocalDate endDate = cds.getMaturityDate().toLocalDate();

    ISDAPremiumLegSchedule paymentSchedule = new ISDAPremiumLegSchedule(startDate, endDate, cds.getCouponFrequency().getPeriod(), cds.getStubType(), cds.getBusinessDayAdjustmentConvention(),
        cds.getCalendar(), cds.getProtectionStart());
    final int nPayments = paymentSchedule.getNumPayments();

    // these are potentially different from startDate and endDate
    final LocalDate globalAccStart = paymentSchedule.getAccStartDate(0);
    final LocalDate golobalAccEnd = paymentSchedule.getAccEndDate(nPayments - 1);

    // TODO this logic could be part of ISDAPremiumLegSchdule
    final LocalDate matDate = cds.getProtectionStart() ? golobalAccEnd.minusDays(1) : golobalAccEnd;

    if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {
      return 0.0; // trade has expired
    }

    final LocalDate[] yieldCurveDates = toLocalDate(yieldCurve.getCurveDates());
    final LocalDate[] creditCurveDates = toLocalDate(hazardRateCurve.getCurveTenors());
    LocalDate[] integrationSchedule = getIntegrationNodesAsDates(globalAccStart, golobalAccEnd, yieldCurveDates, creditCurveDates);

    // Build the integration schedule for the calculation of the accrued leg (this is not a cashflow schedule per se, but a set of time nodes for evaluating the accrued payment integral)
    // final ZonedDateTime[] accruedLegIntegrationSchedule = accruedLegScheduleBuilder.constructCreditDefaultSwapAccruedLegIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, false);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // The survival curve is defined as to the end of day. If the observation is at the start of day we subtract one.
    if (cds.getProtectionStart()) {
      obsOffset = -1;
    }

    for (int i = 0; i < nPayments; i++) {

      // this loop mimics the ISCA c function FreePaymentPVWithTimeLine

      final LocalDate accStart = paymentSchedule.getAccStartDate(i);
      final LocalDate accEnd = paymentSchedule.getAccEndDate(i);
      final LocalDate pay = paymentSchedule.getPaymentDate(i);

      // final ZonedDateTime accrualStartDate = premiumLegSchedule[i][1];
      // final ZonedDateTime accrualEndDate = premiumLegSchedule[i][2]; // accuralEnd and payDate are 1 less than in ISDA c
      // final ZonedDateTime payDate = premiumLegSchedule[i][3];

      if (!accEnd.isAfter(stepinDate)) {
        continue; // this cashflow has already been realised
      }

      // TODO should not be hard-coded to ACT/360
      final double accTime = ACT_360.getDayCountFraction(accStart, accEnd);

      // time relevant for discounting - discount curve also uses ACT/365
      double t = ACT_365.getDayCountFraction(today, pay);

      double tObsOffset = ACT_365.getDayCountFraction(today, accEnd.plusDays(obsOffset));

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
      // This mimics the ISDA c function JpmcdsAccrualOnDefaultPVWithTimeLine
      if (cds.getIncludeAccruedPremium()) {

        final LocalDate offsetStepinDate = stepinDate.plusDays(obsOffset);
        final LocalDate offsetAccStartDate = accStart.plusDays(obsOffset);
        final LocalDate offsetAccEndDate = accEnd.plusDays(obsOffset);

        LocalDate[] truncatedDateList = truncateList(offsetAccStartDate, offsetAccEndDate, integrationSchedule);
        final int nItems = truncatedDateList.length;

        // max(offsetStepinDate,offsetAccStartDate)
        LocalDate subStartDate = offsetStepinDate.isAfter(offsetAccStartDate) ? offsetStepinDate : offsetAccStartDate;
        double tAcc = ACT_365.getDayCountFraction(offsetAccStartDate, offsetAccEndDate);

        final double accRate = accTime / tAcc;
        t = ACT_365.getDayCountFraction(today, subStartDate);

        // Compensate Java shortcoming
        if (Double.compare(t, -0.0) == 0) {
          t = 0;
        }
        double s0 = hazardRateCurve.getSurvivalProbability(t);
        double df0 = yieldCurve.getDiscountFactor(t);

        for (int j = 1; j < nItems; ++j) {

          if (!truncatedDateList[j].isAfter(offsetStepinDate)) {
            continue;
          }

          double thisAccPV = 0.0;
          t = ACT_365.getDayCountFraction(today, truncatedDateList[j]);
          final double s1 = hazardRateCurve.getSurvivalProbability(t);
          final double df1 = yieldCurve.getDiscountFactor(t);

          final double t0 = ACT_365.getDayCountFraction(offsetAccStartDate, subStartDate) + 1 / 730.; // add on half a day
          final double t1 = ACT_365.getDayCountFraction(offsetAccStartDate, truncatedDateList[j]) + 1 / 730.;
          t = t1 - t0; // t repurposed

          // TODO check for s0 == s1 -> zero prob of default (and thus zero PV contribution) from this section
          // if (s0 == s1) {
          // continue;
          // }

          final double lambda = Math.log(s0 / s1) / t;
          final double fwdRate = Math.log(df0 / df1) / t;
          final double lambdafwdRate = lambda + fwdRate + 1.0e-50;

          thisAccPV = lambda * accRate * s0 * df0 * ((t0 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) - (t1 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) * s1 / s0 * df1 / df0);
          myPV += thisAccPV;
          s0 = s1;
          df0 = df1;
          subStartDate = truncatedDateList[j];
        }

      } // end if acc fee payment

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
      presentValuePremiumLeg -= calculateAccruedInterest(cds.getDayCountFractionConvention(), paymentSchedule, stepinDate);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return cds.getNotional() * presentValuePremiumLeg;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // Method to calculate the accrued interest between the last accrual date and the current valuation date

  private double calculateAccruedInterest(final DayCount dayCount, final ISDAPremiumLegSchedule premiumLegSchedule, final LocalDate stepinDate) {

    final int n = premiumLegSchedule.getNumPayments();

    // stepinDate is before first accStart or after last accEnd
    if (!stepinDate.isAfter(premiumLegSchedule.getAccStartDate(0)) || !stepinDate.isBefore(premiumLegSchedule.getAccEndDate(n - 1))) {
      return 0.0;
    }

    int index = premiumLegSchedule.getAccStartDateIndex(stepinDate);
    if (index >= 0) {
      return 0.0; // on accrual start date
    }

    index = -(index + 1); // binary search notation

    if (index == 0) {
      throw new MathException("Error in calculateAccruedInterest - check logic"); // this should never be hit
    }

    return dayCount.getDayCountFraction(premiumLegSchedule.getAccStartDate(index - 1), stepinDate);
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

    if (!startDate.isAfter(valuationDate.minusDays(1))) {
      startDate = valuationDate.minusDays(1);
    }

    if (valuationDate.isAfter(clEndDate)) {
      presentValueContingentLeg = 0.0;
      return presentValueContingentLeg;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Build the integration schedule for the calculation of the contingent leg (this is not a cashflow schedule per se, but a set of timenodes for evaluating the contingent leg integral)
    // final double[] contingentLegIntegrationSchedule = contingentLegScheduleBuilder.constructCreditDefaultSwapContingentLegIntegrationSchedule(valuationDate, startDate, clEndDate, cds, yieldCurve,
    // hazardRateCurve);

    final double[] contingentLegIntegrationSchedule = ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(valuationDate, startDate, clEndDate, yieldCurve.getCurveDates(),
        hazardRateCurve.getCurveTenors());

    double s1 = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[0]);
    double df1 = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[0]);
    final double lossGivenDefault = (1 - cds.getRecoveryRate());

    for (int i = 1; i < contingentLegIntegrationSchedule.length; ++i) {
      final double dt = contingentLegIntegrationSchedule[i] - contingentLegIntegrationSchedule[i - 1];
      final double s0 = s1;
      final double df0 = df1;
      s1 = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[i]);
      df1 = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[i]);

      // TODO handle abs(s0-s1) -> and dt -> 0 cases by taking proper limits
      if (s0 == s1) {
        continue; // there is no chance of a default over this period
      }
      final double hazardRate = Math.log(s0 / s1) / dt;
      final double interestRate = Math.log(df0 / df1) / dt;

      presentValueContingentLeg += lossGivenDefault * (hazardRate / (hazardRate + interestRate)) * (1.0 - Math.exp(-(hazardRate + interestRate) * dt)) * s0 * df0;
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
