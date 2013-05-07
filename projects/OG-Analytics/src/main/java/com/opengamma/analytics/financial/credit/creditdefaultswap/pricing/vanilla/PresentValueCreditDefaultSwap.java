/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapIntegrationSchedule;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing the methods for valuing a CDS which are common to all types of CDS e.g. the contingent leg
 * calculation
 */
public class PresentValueCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final int spotDays = 0;

  private static final boolean businessDayAdjustCashSettlementDate = true;

  private static final BusinessDayConvention cashSettlementDateBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  private static final int DEFAULT_N_POINTS = 30;
  private final int _numberOfIntegrationSteps;

  private static final double spreadLowerBound = 1e-10;
  private static final double spreadUpperBound = 1e10;

  public PresentValueCreditDefaultSwap() {
    this(DEFAULT_N_POINTS);
  }

  public PresentValueCreditDefaultSwap(final int numberOfIntegrationPoints) {
    _numberOfIntegrationSteps = numberOfIntegrationPoints;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : The code in this class is a complete mess at the moment - will be completely rewritten and modularised 

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Add a method to calc both the legs in one method (useful for performance reasons e.g. not computing survival probabilities and discount factors twice)
  // TODO : If valuationDate = adjustedMatDate - 1day have to be more careful in how the contingent leg integral is calculated
  // TODO : Fix the bug when val date is very close to mat date
  // TODO : Need to add the code for when the settlement date > 0 business days (just a discount factor)
  // TODO : Replace the while with a binary search function
  // TODO : Should build the cashflow schedules outside of the leg valuation routines to avoid repitition of calculations
  // TODO : Eventually replace the ISDACurve with a YieldCurve object (currently using ISDACurve built by RiskCare as this allows exact comparison with the ISDA model)
  // TODO : Replace the accrued schedule double with a ZonedDateTime object to make it consistent with other calculations
  // TODO : Tidy up the calculatePremiumLeg, valueFeeLegAccrualOnDefault and methods
  // TODO : Add the calculation for the settlement and stepin discount factors
  // TODO : Need to add the PROT_PAY_MAT option as well

  // TODO : when the ISDA calibration routine fails, then should fall back to the simple bi-section that was originally implemented
  // TODO : since this routine very rarely falls over

  // TODO : Add the calculation of the cash settlement amount

  // ----------------------------------------------------------------------------------------------------------------------------------------

  /*
  public double calculateISDACompliantPremiumLeg(final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    final double presentValuePremiumLeg = 0.0;
    final double presentValueAccruedInterest = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct a cashflow schedule object for the premium leg
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return cds.getNotional() * (presentValuePremiumLeg + presentValueAccruedInterest);
  }
  */

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS (with a hazard rate curve calibrated to market observed data)

  // The code for the accrued calc has just been lifted from RiskCare's implementation for now because it exactly reproduces
  // the ISDA model - will replace with a better model in due course

  @Deprecated
  public double calculatePremiumLeg(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Local variable definitions
    //final int startIndex = 0;

    double presentValuePremiumLeg = 0.0;
    //final double presentValueAccruedInterest = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct a cashflow schedule object for the premium leg
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    //final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    //final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime[][] premiumLegSchedule = cashflowSchedule.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);

    // Construct a schedule object for the accrued leg (this is not a cashflow schedule per se, but a set of time nodes for evaluating the accrued payment integral)
    final GenerateCreditDefaultSwapIntegrationSchedule accruedSchedule = new GenerateCreditDefaultSwapIntegrationSchedule();

    // Build the integration schedule for the calculation of the accrued leg
    final ZonedDateTime[] accruedLegIntegrationSchedule = accruedSchedule.constructCreditDefaultSwapAccruedLegIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, false);

    // Calculate the stepin time with the appropriate offset
    //final double offsetStepinTime = accruedSchedule.calculateCreditDefaultSwapOffsetStepinTime(valuationDate, cds, ACT_365);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Get the (adjusted) maturity date of the trade
    final ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);
    //final ZonedDateTime startDate = premiumLegSchedule[0];
    //final ZonedDateTime startDate = premiumLegSchedule[0][0];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Is this check necessary and/or correct?
    // If the valuationDate is after the adjusted maturity date then throw an exception (differs from check in ctor because of the adjusted maturity date)
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // TODO : Check the effective date calc here
    // If the valuation date is exactly the adjusted maturity date then simply return zero

    /*
        if (valuationDate.equals(adjustedMaturityDate) || cds.getEffectiveDate().equals(adjustedMaturityDate)) {
          return 0.0;
        }
     */

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Determine where in the cashflow schedule the valuationDate is
    //final int startCashflowIndex = getCashflowIndex(valuationDate, premiumLegSchedule, 1, 1);

    final ZonedDateTime today = valuationDate;
    final ZonedDateTime stepinDate = cds.getEffectiveDate(); // TODO this relies on the person that's set up the CDS to know that effective date = 1 day after valuation date by convention

    // The value date is when cash settlement is made
    final ZonedDateTime valueDate = valuationDate;

    // TODO : Add the extra logic for this calculation (safe for the moment since 'protectionStart' is TRUE always)
    final ZonedDateTime matDate = cds.getMaturityDate();

    // TODO : Check valueDate >= today and stepinDate >= today

    // TODO : Check when today > matDate || stepinDate > matDate
    if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {

      presentValuePremiumLeg = 0.0;

      return presentValuePremiumLeg;
    }

    double thisPV = 0.0;

    for (int i = 1; i < premiumLegSchedule.length; i++) {

      // ---------------------------

      // Into FeePaymentPVWithTimeLine

      int obsOffset = 0;

      if (cds.getProtectionStart()) {
        obsOffset = -1;
      }

      //final ZonedDateTime accrualStartDate = premiumLegSchedule[i - 1];
      //ZonedDateTime accrualEndDate = premiumLegSchedule[i];

      //final ZonedDateTime accrualStartDate = premiumLegSchedule[i - 1][0];
      //ZonedDateTime accrualEndDate = premiumLegSchedule[i][0];

      final ZonedDateTime accrualStartDate = premiumLegSchedule[i][1];
      ZonedDateTime accrualEndDate = premiumLegSchedule[i][2];
      final ZonedDateTime payDate = premiumLegSchedule[i][3];

      if (!accrualEndDate.isAfter(stepinDate)) {
        continue;
      }

      // The last coupon date has an extra day of accrued
      if (i == premiumLegSchedule.length - 1) {

        // REMEMBER HAVE COMMENTED THIS OUT

        //accrualEndDate = accrualEndDate.plusDays(1);

      }

      double delta = 1.0;

      /*
      final boolean temp = accrualEndDate.isAfter(stepinDate);

      // TODO : Check accEndDate <= stepinDate
      if (temp == false) {
        delta = 0.0;
      }
       */

      final double accTime = TimeCalculator.getTimeBetween(accrualStartDate, accrualEndDate, ACT_360);

      //ZonedDateTime discountDate = accrualEndDate;

      /*
      if (i == premiumLegSchedule.length - 1) {

        //obsOffset = 0;

        // REMEMBER HAVE COMMENTED THIS OUT
        //discountDate = accrualEndDate.minusDays(1);

        //accTime = TimeCalculator.getTimeBetween(accrualStartDate, accrualEndDate.plusDays(1), ACT_360);
      }
      */

      double tObsOffset = TimeCalculator.getTimeBetween(today, accrualEndDate.plusDays(obsOffset), ACT_365);
      if (Double.compare(tObsOffset, -0.0) == 0) {
        tObsOffset = 0;
      }

      //double t = TimeCalculator.getTimeBetween(today, accrualEndDate, ACT_365);

      //double t = TimeCalculator.getTimeBetween(today, discountDate, ACT_365);

      double t = TimeCalculator.getTimeBetween(today, payDate, ACT_365);

      final double survival = hazardRateCurve.getSurvivalProbability(tObsOffset);

      final double discount = yieldCurve.getDiscountFactor(t);

      //final double tStart = TimeCalculator.getTimeBetween(today, today, ACT_365);

      //final double discount = yieldCurve.getDiscountFactor(today, accrualEndDate);

      final double amount = accTime * 0.050000000000000003;
      final double tempPV = accTime * discount * survival * 0.050000000000000003;

      thisPV += delta * accTime * discount * survival;

      // ---------------------------------------------

      double myPV = 0.0;

      if (cds.getIncludeAccruedPremium()) {

        final double accrual = 0.0;

        final ZonedDateTime offsetStepinDate = stepinDate.plusDays(obsOffset);            // stepinDate
        final ZonedDateTime offsetAccStartDate = accrualStartDate.plusDays(obsOffset);    // startDate
        final ZonedDateTime offsetAccEndDate = accrualEndDate.plusDays(obsOffset);        // endDate

        // TODO : Check endDate > startDate

        final ZonedDateTime[] truncatedDateList = accruedSchedule.getTruncatedTimeLineDeprecated(accruedLegIntegrationSchedule, offsetAccStartDate, offsetAccEndDate);

        ZonedDateTime subStartDate;

        if (offsetStepinDate.isAfter(offsetAccStartDate)) {
          subStartDate = offsetStepinDate;
        } else {
          subStartDate = offsetAccStartDate;
        }

        final double tAcc = TimeCalculator.getTimeBetween(offsetAccStartDate, offsetAccEndDate, ACT_365);

        final double accRate = accTime / tAcc;

        t = TimeCalculator.getTimeBetween(today, subStartDate, ACT_365);

        if (Double.compare(t, -0.0) == 0) {
          t = 0;
        }
        double s0 = hazardRateCurve.getSurvivalProbability(t);
        double df0 = yieldCurve.getDiscountFactor(t);

        //double df0 = yieldCurve.getDiscountFactor(today, subStartDate);

        for (int j = 1; j < truncatedDateList.length; ++j) {

          double thisAccPV = 0.0;

          // Check this
          if (!truncatedDateList[j].isAfter(offsetStepinDate)) {

          } else {

            t = TimeCalculator.getTimeBetween(today, truncatedDateList[j], ACT_365);
            final double s1 = hazardRateCurve.getSurvivalProbability(t);
            final double df1 = yieldCurve.getDiscountFactor(t);

            //double df1 = yieldCurve.getDiscountFactor(today, truncatedDateList[j]);

            final double t0 = TimeCalculator.getTimeBetween(offsetAccStartDate, subStartDate, ACT_365) + 0.5 / 365.0;
            final double t1 = TimeCalculator.getTimeBetween(offsetAccStartDate, truncatedDateList[j], ACT_365) + 0.5 / 365.0;
            t = t1 - t0;
            final double lambda = Math.log(s0 / s1) / t;
            final double fwdRate = Math.log(df0 / df1) / t;
            final double lambdafwdRate = lambda + fwdRate + 1.0e-50;

            thisAccPV = lambda * accRate * s0 * df0 * ((t0 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) - (t1 + 1.0 / (lambdafwdRate)) / (lambdafwdRate) * s1 / s0 * df1 / df0);
            myPV += thisAccPV;
            s0 = s1;
            df0 = df1;
            subStartDate = truncatedDateList[j];
          }

        }
      } // end if acc fee payment

      // ---------------------------------------------

      thisPV += myPV;

    } // end loop over fee leg payments

    presentValuePremiumLeg = thisPV;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Check this calculation - maybe move it out of this routine and into the PV calculation routine?
    // TODO : Note the cash settlement date is hardcoded at 3 days

    // TODO : Need to make sure that the bda is being done correctly (need to check it wherever the cashsettle adjustment is performed)

    //final int spotDays = 5;

    ZonedDateTime bdaCashSettlementDate = valuationDate.plusDays(spotDays);

    if (businessDayAdjustCashSettlementDate) {
      bdaCashSettlementDate = cashSettlementDateBusinessDayConvention.adjustDate(cds.getCalendar(), valuationDate.plusDays(spotDays));
    }

    //final double tSett = TimeCalculator.getTimeBetween(valuationDate, valuationDate.plusDays(spotDays));

    final double tSett = TimeCalculator.getTimeBetween(valuationDate, bdaCashSettlementDate);

    final double valueDatePV = yieldCurve.getDiscountFactor(tSett);

    presentValuePremiumLeg /= valueDatePV;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    if (priceType == PriceType.CLEAN) {

      // pass in stepinDate as 'today' (this is what it is called in FeeLegAI in the ISDA code)

      double ai = 0.0;

      // TODO : Maybe check if stepinDate is in range [startDate, maturityDate + 1] - probably not necessary since the valuation will not allow this

      //final int startCashflowIndex = getCashflowIndex(stepinDate, premiumLegSchedule, 0, 1);

      /*
      ZonedDateTime rollingDate = premiumLegSchedule[0].minusDays(1);
      while (rollingDate.isBefore(premiumLegSchedule[startCashflowIndex])) {
        startCashflowIndex++;
        rollingDate = premiumLegSchedule[startCashflowIndex];
      }
       */

      // Start at the beginning of the cashflow schedule
      //ZonedDateTime rollingDate = premiumLegSchedule[0];
      ZonedDateTime rollingDate = premiumLegSchedule[0][0];
      //ZonedDateTime rollingDate = premiumLegSchedule[1][1];

      double deltaai = 1.0;

      int startCashflowIndex = 0;
      //int startCashflowIndex = 1;

      // step through the cashflow schedule until we get to the step in date
      while (rollingDate.isBefore(stepinDate)) {
        startCashflowIndex++;
        //rollingDate = premiumLegSchedule[startCashflowIndex];
        rollingDate = premiumLegSchedule[startCashflowIndex][0];
        //rollingDate = premiumLegSchedule[startCashflowIndex][1];
      }

      // Get the date of the last coupon before the current valuation date
      //final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1];
      final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1][0];
      //final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1][1];

      // Compute the amount of time between previousPeriod and stepinDate
      final double dcf = cds.getDayCountFractionConvention().getDayCountFraction(previousPeriod, stepinDate);

      ai = dcf;

      // Calculate the accrued interest gained in this period of time

      //if (rollingDate.equals(stepinDate)) {
      //ai = 0.0;
      //} else {
      // ai = /*(cds.getParSpread() / 10000.0) * */dcf;
      //}

      presentValuePremiumLeg -= ai;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return cds.getNotional() * presentValuePremiumLeg;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Need to re-write this code completely - it is completely terrible!!

  private double valueFeeLegAccrualOnDefault(
      final double amount,
      final double[] timeline,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final int startIndex,
      final int endIndex,
      final double stepinTime,
      final double stepinDiscountFactor) {

    final double[] timePoints = timeline; //timeline.getTimePoints();

    final double startTime = timePoints[startIndex];
    final double endTime = timePoints[endIndex];

    final double subStartTime = stepinTime > startTime ? stepinTime : startTime;
    final double accrualRate = amount / (endTime - startTime);

    double t0, t1, dt, survival0, survival1, discount0, discount1;
    double lambda, fwdRate, lambdaFwdRate, valueForTimeStep, value;

    t0 = subStartTime - startTime + 0.5 * (1.0 / 365.0); //HALF_DAY_ACT_365F;

    survival0 = hazardRateCurve.getSurvivalProbability(subStartTime);

    final double PRICING_TIME = 0.0;

    discount0 = startTime < stepinTime || startTime < PRICING_TIME ? stepinDiscountFactor : yieldCurve.getDiscountFactor(timePoints[startIndex]); //discountFactors[startIndex];

    value = 0.0;

    for (int i = startIndex + 1; i <= endIndex; ++i) {

      if (timePoints[i] <= stepinTime) {
        continue;
      }

      t1 = timePoints[i] - startTime + 0.5 * (1.0 / 365.0); //HALF_DAY_ACT_365F;
      dt = t1 - t0;

      survival1 = hazardRateCurve.getSurvivalProbability(timePoints[i]);
      discount1 = yieldCurve.getDiscountFactor(timePoints[i]); //discountFactors[i];

      lambda = Math.log(survival0 / survival1) / dt;
      fwdRate = Math.log(discount0 / discount1) / dt;
      lambdaFwdRate = lambda + fwdRate + 1.0e-50;
      valueForTimeStep = lambda * accrualRate * survival0 * discount0
          * (((t0 + 1.0 / lambdaFwdRate) / lambdaFwdRate) - ((t1 + 1.0 / lambdaFwdRate) / lambdaFwdRate) * survival1 / survival0 * discount1 / discount0);

      value += valueForTimeStep;

      t0 = t1;

      survival0 = survival1;
      discount0 = discount1;
    }

    return value;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // If the cleanPrice flag is TRUE then this function is called to calculate the accrued interest between valuationDate and the previous coupon date

  @Deprecated
  public double calculateAccruedInterest(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds) {

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // Assume the stepin date is the valuation date + 1 day (this is not business day adjusted)
    final ZonedDateTime stepinDate = valuationDate.plusDays(1);

    // Determine where in the premium leg cashflow schedule the current valuation date is
    final int startCashflowIndex = getCashflowIndex(valuationDate, premiumLegSchedule, 0, 1);

    // Get the date of the last coupon before the current valuation date
    final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1];

    // Compute the amount of time between previousPeriod and stepinDate
    final double dcf = cds.getDayCountFractionConvention().getDayCountFraction(previousPeriod, stepinDate);

    // Calculate the accrued interest gained in this period of time
    final double accruedInterest = /*(cds.getParSpread() / 10000.0) * */dcf * cds.getNotional();

    return accruedInterest;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to determine where in the premium leg cashflow schedule the valuation date is

  private int getCashflowIndex(
      final ZonedDateTime valuationDate,
      final ZonedDateTime[] premiumLegSchedule,
      final int startIndex,
      final int deltaDays) {

    int counter = startIndex;

    // Determine where in the cashflow schedule the valuationDate is
    while (!valuationDate.isBefore(premiumLegSchedule[counter].minusDays(deltaDays))) {
      counter++;
    }

    return counter;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the contingent leg (replicates the calculation in the ISDA model)

  @Deprecated
  public double calculateContingentLeg(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Check if valDate > matDate and return zero if so

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Remember that the start date for protection to begin is MAX(stepinDate, startDate)

    // Local variable definitions
    double presentValueContingentLeg = 0.0;

    int offset = 0;

    if (cds.getProtectionStart()) {
      offset = 1;
    }

    ZonedDateTime startDate;
    ZonedDateTime clStartDate = valuationDate;
    final ZonedDateTime clEndDate = cds.getMaturityDate();

    // NOTE :
    if (cds.getProtectionStart()) {
      clStartDate = valuationDate.minusDays(1);
    }

    final ZonedDateTime stepinDate = cds.getEffectiveDate();

    if (clStartDate.isAfter(stepinDate.minusDays(offset))) {
      startDate = clStartDate;
    } else {
      startDate = stepinDate.minusDays(offset);
    }

    if (startDate.isAfter(valuationDate.minusDays(1))) {
      //startDate = startDate;
    } else {
      startDate = valuationDate.minusDays(1);
    }

    if (valuationDate.isAfter(clEndDate)) {
      presentValueContingentLeg = 0.0;
      return presentValueContingentLeg;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct an integration schedule object for the contingent leg
    final GenerateCreditDefaultSwapIntegrationSchedule contingentLegSchedule = new GenerateCreditDefaultSwapIntegrationSchedule();

    // Build the integration schedule for the calculation of the contingent leg
    final double[] contingentLegIntegrationSchedule = contingentLegSchedule.constructCreditDefaultSwapContingentLegIntegrationSchedule(valuationDate, startDate, clEndDate, cds, yieldCurve,
        hazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Get the survival probability at the first point in the integration schedule
    double survivalProbability = hazardRateCurve.getSurvivalProbability(contingentLegIntegrationSchedule[0]);

    // Get the discount factor at the first point in the integration schedule
    double discountFactor = yieldCurve.getDiscountFactor(contingentLegIntegrationSchedule[0]);

    final double loss = (1 - cds.getRecoveryRate());

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop over each of the points in the integration schedule
    for (int i = 1; i < contingentLegIntegrationSchedule.length; ++i) {

      // Calculate the time between adjacent points in the integration schedule
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

      // Calculate the contribution of the interval deltat to the overall contingent leg integral
      presentValueContingentLeg += loss * (hazardRate / (hazardRate + interestRate)) * (1.0 - Math.exp(-(hazardRate + interestRate) * deltat)) * survivalProbabilityPrevious * discountFactorPrevious;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Check this calculation - maybe move it out of this routine and into the PV calculation routine?
    // TODO : Note the cash settlement date is hardcoded at 3 days

    //final int spotDays = 5;

    //final ZonedDateTime cashSettleDate = valuationDate.plusDays(spotDays);
    //final double t = TimeCalculator.getTimeBetween(valuationDate, cashSettleDate, ACT_365);

    ZonedDateTime bdaCashSettlementDate = valuationDate.plusDays(spotDays);

    if (businessDayAdjustCashSettlementDate) {
      bdaCashSettlementDate = cashSettlementDateBusinessDayConvention.adjustDate(cds.getCalendar(), valuationDate.plusDays(spotDays));
    }

    final double t = TimeCalculator.getTimeBetween(valuationDate, bdaCashSettlementDate, ACT_365);

    final double valueDatePV = yieldCurve.getDiscountFactor(t);

    return cds.getNotional() * presentValueContingentLeg / valueDatePV;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the value of the contingent leg of a CDS (with a hazard rate curve calibrated to market observed data) - Currently not used but this is a more elegant calc than ISDA

  private double calculateContingentLegOld(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct a schedule generation object (to access the adjusted maturity date method)
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Get the date when protection ends
    final ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // If the valuationDate is after the adjusted maturity date then throw an exception (differs from check in ctor because of the adjusted maturity date)
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // If the valuation date is exactly the adjusted maturity date then simply return zero
    if (valuationDate.equals(adjustedMaturityDate)) {
      return 0.0;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double presentValueContingentLeg = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the partition of the time axis for the calculation of the integral in the contingent leg

    // The period of time for which protection is provided
    final double protectionPeriod = TimeCalculator.getTimeBetween(valuationDate, adjustedMaturityDate.plusDays(1), /*cds.getDayCountFractionConvention()*/ACT_365);

    // Given the protection period, how many partitions should it be divided into
    final int numberOfPartitions = (int) (_numberOfIntegrationSteps * protectionPeriod + 0.5);

    // The size of the time increments in the calculation of the integral
    final double epsilon = protectionPeriod / numberOfPartitions;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the integral for the contingent leg (note the limits of the loop)
    for (int k = 1; k <= numberOfPartitions; k++) {

      final double t = k * epsilon;
      final double tPrevious = (k - 1) * epsilon;

      final double discountFactor = yieldCurve.getDiscountFactor(t);

      final double survivalProbability = hazardRateCurve.getSurvivalProbability(t);
      final double survivalProbabilityPrevious = hazardRateCurve.getSurvivalProbability(tPrevious);

      presentValueContingentLeg += discountFactor * (survivalProbabilityPrevious - survivalProbability);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return cds.getNotional() * (1.0 - cds.getRecoveryRate()) * presentValueContingentLeg;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to move this function

  // Given a points upfront amount, compute the flat par spread implied by this
  public double calculateParSpreadFlat(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final double upfrontAmount,
      final ZonedDateTime[] marketTenors,
      final ISDADateCurve yieldCurve,
      final PriceType priceType) {

    // 1 x 1 vector to hold the flat spread (term structure)
    final double[] marketSpreads = new double[1];

    final Function1D<Double, Double> function = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double parSpread) {

        // For this value of the flat spread, compute the upfront amount
        marketSpreads[0] = parSpread;
        final double pointsUpfront = calculateUpfrontFlat(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

        // Compute the difference between the calculated and input upfront amount
        final double delta = pointsUpfront - upfrontAmount;

        return delta;
      }
    };

    final double parSpreadFlat = new BisectionSingleRootFinder().getRoot(function, spreadLowerBound, spreadUpperBound);

    return parSpreadFlat;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to move this function

  // Calculate the upfront amount given a specified spread curve level
  public double calculateUpfrontFlat(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Add arg checkers

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vectors of time nodes and spreads for the hazard rate curve (note the sizes of these arrays)
    final double[] times = new double[1];

    final double[] spreads = new double[1];

    times[0] = ACT_365.getDayCountFraction(valuationDate, marketTenors[0]);
    spreads[0] = marketSpreads[0];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS for calibration
    final LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    // Create a CDS for valuation
    final LegacyVanillaCreditDefaultSwapDefinition valuationCDS = cds;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Call the constructor to create a CDS present value object
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Call the constructor to create a calibrate hazard rate curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // ----------------------------------------------------------------------------------------------------------------------------------------

    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, marketTenors, marketSpreads, yieldCurve, PriceType.CLEAN);

    final double[] modifiedHazardRateCurve = new double[1];

    modifiedHazardRateCurve[0] = calibratedHazardRates[0];

    // Build a hazard rate curve object based on the input market data
    final HazardRateCurve calibratedHazardRateCurve = new HazardRateCurve(marketTenors, times, modifiedHazardRateCurve/*calibratedHazardRates*/, 0.0);

    final double pointsUpfront = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, valuationCDS, yieldCurve, calibratedHazardRateCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return pointsUpfront / cds.getNotional();
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to move this function

  @Deprecated
  public double calibrateAndGetPresentValue(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve,
      final PriceType priceType) {

    // Create a CDS for valuation
    final LegacyVanillaCreditDefaultSwapDefinition valuationCDS = cds;

    // Call the constructor to create a CDS present value object
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Build a hazard rate curve object based on the input market data
    final HazardRateCurve calibratedHazardRateCurve = calibrateHazardRateCurve(valuationDate, valuationCDS, marketTenors, marketSpreads, yieldCurve);

    // Calculate the CDS PV using the just calibrated hazard rate term structure
    final double presentValue = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, valuationCDS, yieldCurve, calibratedHazardRateCurve, priceType);

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double newCalibrateAndGetPresentValue(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve,
      final PriceType priceType) {

    // Create a CDS for valuation
    final LegacyVanillaCreditDefaultSwapDefinition valuationCDS = cds;

    // Call the constructor to create a CDS present value object
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Build a hazard rate curve object based on the input market data
    final HazardRateCurve calibratedHazardRateCurve = newCalibrateHazardRateCurve(valuationDate, valuationCDS, marketTenors, marketSpreads, yieldCurve);

    /*
    final int numberOfDays = 3000;
    ZonedDateTime rollingDate;
    for (int i = 0; i < numberOfDays; i++) {
      rollingDate = valuationDate.plusDays(10000 + i);
      final double t = TimeCalculator.getTimeBetween(valuationDate, rollingDate, ACT_365);
      final double survivalProbability = calibratedHazardRateCurve.getSurvivalProbability(t);
      System.out.println("i = " + "\t" + i + "\t" + rollingDate + "\t" + survivalProbability);
    }
    */

    // Calculate the CDS PV using the just calibrated hazard rate term structure
    final double presentValue = creditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, valuationCDS, yieldCurve, calibratedHazardRateCurve, priceType);

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to change the type of CDS object passed in (only passing in a legacy CDS so we can access its par spread)

  private double cdsBootstrapPointFunction(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // NOTE : We divide the values returned from the premium and contingent leg calculations by the trade notional. This is only for the purposes of the calibration
    // NOTE : routine because we require a unit notional (so that the comparison with the accuracy variables are meaningful)

    final double premLeg = (cds.getParSpread() / 10000) * calculatePremiumLeg(valuationDate, cds, yieldCurve, hazardRateCurve, PriceType.CLEAN) / cds.getNotional();
    final double contLeg = calculateContingentLeg(valuationDate, cds, yieldCurve, hazardRateCurve) / cds.getNotional();

    final double pv = contLeg - premLeg;

    return pv;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to change the type of CDS object passed in

  // TODO : This code is simply adapted from the ISDA code. No attempt has been made to make it more logical or correct e.g. using Double.tobits

  private double jpmCDSRootFindBrent(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final double guess,
      final PriceType priceType) {

    // ------------------------------

    final double boundLo = 0.0;
    final double boundHi = 1e10;
    final int numIterations = 100;
    double initialXstep = 0.0005;
    final double initialFDeriv = 0;
    final double xacc = 1e-10;
    final double facc = 1e-10;
    final double ONE_PER_CENT = 0.01;

    // ------------------------------

    double[] xPoints = new double[3];
    double[] yPoints = new double[3];

    xPoints[0] = guess;

    // ------------------------------

    if (boundLo >= boundHi) {
      // TODO : Throw an exception here
    }

    if (xPoints[0] < boundLo || xPoints[0] > boundHi) {
      // TODO : Throw an exception here
    }

    // Calc the value of the objective function at the initial hazard rate guess i.e. using the hazard rate curve as input
    yPoints[0] = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, hazardRateCurve, priceType);

    if (yPoints[0] == 0.0 || (Math.abs(yPoints[0]) <= facc && (Math.abs(boundLo - xPoints[0]) <= xacc || Math.abs(boundHi - xPoints[0]) <= xacc))) {
      return xPoints[0];
    }

    // ------------------------------

    double boundSpread = boundHi - boundLo;

    if (initialXstep == 0.0) {
      initialXstep = ONE_PER_CENT * boundSpread;
    }

    // ------------------------------

    if (initialFDeriv == 0.0) {
      xPoints[2] = xPoints[0] + initialXstep;
    } else {
      xPoints[2] = xPoints[0] - yPoints[0] / initialFDeriv;
    }

    // Begin if
    if (xPoints[2] < boundLo || xPoints[2] > boundHi) {

      xPoints[2] = xPoints[0] - initialXstep;

      if (xPoints[2] < boundLo) {
        xPoints[2] = boundLo;
      }
      if (xPoints[2] > boundHi) {
        xPoints[2] = boundHi;
      }

      if (xPoints[2] == xPoints[0]) {
        if (xPoints[2] == boundLo) {
          xPoints[2] = boundLo + ONE_PER_CENT * boundSpread;
        } else {
          xPoints[2] = boundHi - ONE_PER_CENT * boundSpread;
        }
      }
    }
    // End if

    // ------------------------------

    HazardRateCurve modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, xPoints[2]);

    yPoints[2] = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, priceType);

    if (yPoints[2] == 0.0 || (Math.abs(yPoints[2]) <= facc && Math.abs(xPoints[2] - xPoints[0]) <= xacc)) {
      return xPoints[2];
    }

    // ------------------------------

    // This is terrible code, but it has to be absolutely comparable with the ISDA model calcs otherwise  
    // we will get small differences in the calibrated hazard rates

    final double[] secantSearch = secant(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, numIterations, xacc, facc, boundLo, boundHi, xPoints, yPoints);

    if (secantSearch[1] == 1.0)
    {
      // Found the root
      return secantSearch[2];
    }
    else if (secantSearch[0] == 1.0)
    {
      // Didn't find the root, but it was bracketed

      // Do the Brent method call here

      // Do we pass in the modifiedHazardRateCurve ?
      final double root = brentMethod(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, numIterations, xacc, facc, xPoints, yPoints);

      return root;
    }
    else
    {
      // Root was not found or bracketed, now try at the bounds

      modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, boundLo);

      final double fLo = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, priceType);

      if (fLo == 0.0 || (Math.abs(fLo) <= facc && Math.abs(boundLo - xPoints[0]) <= xacc)) {
        return boundLo;
      }

      if (yPoints[0] * fLo < 0)
      {
        xPoints[2] = xPoints[0];
        xPoints[0] = boundLo;

        yPoints[2] = yPoints[0];
        yPoints[0] = fLo;
      }
      else
      {
        modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, boundHi);

        final double fHi = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, priceType);

        if (fHi == 0.0 || (Math.abs(fHi) <= facc && Math.abs(boundHi - xPoints[0]) <= xacc))
        {
          return boundHi;
        }

        if (yPoints[0] * fHi < 0)
        {
          xPoints[2] = boundHi;
          yPoints[2] = fHi;
        }
        else
        {
          // Root could not be found - give up
          // TODO : Need to make sure the routine fails more elegantly than this
          return -1;
        }
      } // end if yPoints[0]*fLo < 0

      xPoints[1] = 0.5 * (xPoints[0] + xPoints[2]);

      modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, xPoints[1]);

      yPoints[1] = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, priceType);

      if (yPoints[1] == 0.0 || (Math.abs(yPoints[1]) <= facc && Math.abs(xPoints[1] - xPoints[0]) <= xacc))
      {
        return xPoints[1];
      }

    } // end else root not found or bracketed segment

    // ------------------------------

    double spread = 100.0;

    return spread;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double brentMethod(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final int numIterations,
      final double xacc,
      final double facc,
      final double[] xPoints,
      final double[] yPoints) {

    int j; /* Index */
    double ratio; /* (x3-x1)/(x2-x1) */
    double x31; /* x3-x1*/
    double x21; /* x2-x1*/
    double f21; /* f2-f1 */
    double f31; /* f3-f1 */
    double f32; /* f3-f2 */
    double xm; /* New point found using Brent method*/
    double fm; /* f(xm) */

    double x1 = xPoints[0];
    double x2 = xPoints[1];
    double x3 = xPoints[2];

    double f1 = yPoints[0];
    double f2 = yPoints[1];
    double f3 = yPoints[2];

    for (j = 1; j <= numIterations; j++) {

      if (f2 * f1 > 0.0)
      {
        final double tempX = x1;
        final double tempF = f1;

        x1 = x3;
        x3 = tempX;

        f1 = f3;
        f3 = tempF;
      } // End if f2 * f1 > 0.0

      f21 = f2 - f1;
      f32 = f3 - f2;
      f31 = f3 - f1;
      x21 = x2 - x1;
      x31 = x3 - x1;

      ratio = (x3 - x1) / (x2 - x1);

      if (f3 * f31 < ratio * f2 * f21 || f21 == 0. || f31 == 0. || f32 == 0.)
      {
        x3 = x2;
        f3 = f2;
      }
      else
      {
        xm = x1 - (f1 / f21) * x21 + ((f1 * f2) / (f31 * f32)) * x31 - ((f1 * f2) / (f21 * f32)) * x21;

        HazardRateCurve modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, xm);

        // TODO : Need to pass in the PriceType variable to this calculation
        fm = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, PriceType.CLEAN);

        if (fm == 0.0 || (Math.abs(fm) <= facc && Math.abs(xm - x1) <= xacc))
        {
          return xm;
        }

        if (fm * f1 < 0.0)
        {
          x3 = xm;
          f3 = fm;
        }
        else
        {
          x1 = xm;
          f1 = fm;
          x3 = x2;
          f3 = f2;
        } // End if fm*f1<0.0 else ...

      } // End if f3*f31 < ratio*f2*f21 || f21 == 0. || f31 == 0. || f32 == 0. else ...

      x2 = 0.5 * (x1 + x3);

      HazardRateCurve modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, x2);

      f2 = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, PriceType.CLEAN);

      if (f2 == 0.0 || (Math.abs(f2) <= facc && Math.abs(x2 - x1) <= xacc))
      {
        return x2;
      }

    } // End loop over j

    // TODO : If the algorithm gets here the maximum number of iterations has been exceeded, need to make sure it fails more elegantly
    double root = -1.0;

    return root;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private HazardRateCurve modifyHazardRateCurve(final HazardRateCurve hazardRateCurve, final double h) {

    // Extract out the components of the hazard rate curve
    final ZonedDateTime[] hazardCurveTenors = hazardRateCurve.getCurveTenors();
    final double[] hazardCurveTimes = hazardRateCurve.getTimes();
    final double[] hazardCurveRates = hazardRateCurve.getRates();

    hazardCurveRates[hazardCurveRates.length - 1] = h;

    return hazardRateCurve.bootstrapHelperHazardRateCurve(hazardCurveTenors, hazardCurveTimes, hazardCurveRates);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : This is a bit of a hack because we are passing back TRUE/FALSE information through the return doubles vector

  // results[0] - root bracketed = 1.0
  // results[1] - root found = 1.0
  // results[2] - the root = double

  private double[] secant(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final int numIterations,
      final double xacc,
      final double facc,
      final double boundLo,
      final double boundHi,
      final double[] xPoints,
      final double[] yPoints) {

    double[] result = new double[3];

    //boolean foundIt = false;
    //boolean bracketed = false;

    double dx = 0.0;
    int j = numIterations;

    // TODO : Check this while condition
    // Begin while
    while (j-- > 0) {

      if (Math.abs(yPoints[0]) > Math.abs(yPoints[2])) {

        double tempX = xPoints[0];
        double tempY = yPoints[0];

        xPoints[0] = xPoints[2];
        xPoints[2] = tempX;
        yPoints[0] = yPoints[2];
        yPoints[2] = tempY;
      }

      if (Math.abs(yPoints[0] - yPoints[2]) <= facc)
      {
        if (yPoints[0] - yPoints[2] > 0)
        {
          dx = -yPoints[0] * (xPoints[0] - xPoints[2]) / facc;
        }
        else
        {
          dx = yPoints[0] * (xPoints[0] - xPoints[2]) / facc;
        }
      }
      else
      {
        dx = (xPoints[2] - xPoints[0]) * yPoints[0] / (yPoints[0] - yPoints[2]);
      }

      xPoints[1] = xPoints[0] + dx;

      if (xPoints[1] < boundLo || xPoints[1] > boundHi) {

        result[0] = 0.0;
        result[1] = 0.0;
        result[1] = 0.0;

        return result;
      }

      final HazardRateCurve modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, xPoints[1]);

      yPoints[1] = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, PriceType.CLEAN);

      if (yPoints[1] == 0.0 || (Math.abs(yPoints[1]) <= facc && Math.abs(xPoints[1] - xPoints[0]) <= xacc)) {

        result[0] = 1.0;
        result[1] = 1.0;
        result[2] = xPoints[1];

        return result;
      }

      if ((yPoints[0] < 0 && yPoints[1] < 0 && yPoints[2] < 0) ||
          (yPoints[0] > 0 && yPoints[1] > 0 && yPoints[2] > 0))
      {
        if (Math.abs(yPoints[0]) > Math.abs(yPoints[1]))
        {
          xPoints[2] = xPoints[0];
          yPoints[2] = yPoints[0];
          xPoints[0] = xPoints[1];
          yPoints[0] = yPoints[1];
        }
        else
        {
          xPoints[2] = xPoints[1];
          yPoints[2] = yPoints[1];
        }
        continue;
      }
      else
      {
        if (yPoints[0] * yPoints[2] > 0)
        {
          if (xPoints[1] < xPoints[0])
          {
            double tempX = xPoints[0];
            double tempY = yPoints[0];

            xPoints[0] = xPoints[1];
            xPoints[1] = tempX;

            yPoints[0] = yPoints[1];
            yPoints[1] = tempY;
          }
          else
          {
            double tempX = xPoints[1];
            double tempY = yPoints[1];

            xPoints[1] = xPoints[2];
            xPoints[2] = tempX;

            yPoints[1] = yPoints[2];
            yPoints[2] = tempY;
          }
        }

        result[0] = 1.0;
        result[1] = 0.0;
        result[2] = 0.0;

        return result;
      }
    }
    // End while

    result[0] = 0.0;
    result[1] = 0.0;
    result[2] = 0.0;

    return result;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The ISDA calibration routine

  public HazardRateCurve newCalibrateHazardRateCurve(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve) {

    // ------------------------------

    // TODO : Check the input arguments (not null, market data compatible etc)

    // This is the equivalent of 'CdsBootstrap' function in ISDA code

    // ------------------------------

    // Create a CDS whose maturity we will vary to that of the calibration instrument maturities
    LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    // This vector will store the bootstrapped hazard rates that will be used to construct the calibrated hazard rate term structure object
    double[] calibratedHazardRateCurve = new double[marketTenors.length];

    // tenorsAsDoubles includes time zero (valuationDate)
    final double[] tenorsAsDoubles = new double[marketTenors.length + 1];

    tenorsAsDoubles[0] = 0.0;
    for (int m = 1; m <= marketTenors.length; m++) {
      tenorsAsDoubles[m] = ACT_365.getDayCountFraction(valuationDate, marketTenors[m - 1]);
    }

    // ------------------------------
    // Begin loop over i
    for (int i = 0; i < marketTenors.length; i++) {

      // Remember that the input spreads are in bps, therefore need dividing by 10,000
      final double guess = (marketSpreads[i] / 10000.0) / (1 - cds.getRecoveryRate());

      // Modify the input CDS to have the maturity of the current calibration instrument - this step worries me a bit as there are so many things that can go wrong here
      calibrationCDS = calibrationCDS.withMaturityDate(marketTenors[i]);

      calibrationCDS = calibrationCDS.withSpread(marketSpreads[i]);

      // Now need to build a HazardRateCurve object from the first i calibrated points
      double[] runningTenorsAsDoubles = new double[i + 1];
      double[] runningHazardRates = new double[i + 1];
      ZonedDateTime[] runningMarketTenors = new ZonedDateTime[i + 1];

      calibratedHazardRateCurve[i] = guess;

      for (int m = 0; m <= i; m++) {
        runningMarketTenors[m] = marketTenors[m];
        runningTenorsAsDoubles[m] = ACT_365.getDayCountFraction(valuationDate, runningMarketTenors[m]);
        runningHazardRates[m] = calibratedHazardRateCurve[m];
      }

      // Now build a (running) hazard rate curve for the first i tenors where the hazard rate for tenor i is 'guess'
      HazardRateCurve runningHazardRateCurve = new HazardRateCurve(runningMarketTenors, runningTenorsAsDoubles, runningHazardRates, 0.0);

      // Now calculate the calibrated hazard rate for tenor i (given that the prior tenors have been calibrated) using the ISDA calibration routine
      calibratedHazardRateCurve[i] = jpmCDSRootFindBrent(valuationDate, calibrationCDS, yieldCurve, runningHazardRateCurve, guess, PriceType.CLEAN);

      //System.out.println(i + "\t" + marketTenors[i] + "\t" + calibratedHazardRateCurve[i]);
    }
    // End loop over i
    // ------------------------------

    final double[] modifiedHazardRateCurve = new double[calibratedHazardRateCurve.length + 1];

    modifiedHazardRateCurve[0] = calibratedHazardRateCurve[0];

    for (int m = 1; m < modifiedHazardRateCurve.length; m++) {
      modifiedHazardRateCurve[m] = calibratedHazardRateCurve[m - 1];
    }

    // Now build the complete hazard rate curve
    HazardRateCurve hazardRateCurve = new HazardRateCurve(marketTenors, tenorsAsDoubles, modifiedHazardRateCurve, 0.0);

    return hazardRateCurve;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Deprecated
  public HazardRateCurve calibrateHazardRateCurve(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector of time nodes for the hazard rate curve
    final double[] times = new double[marketTenors.length + 1];

    times[0] = 0.0;
    for (int m = 1; m <= marketTenors.length; m++) {
      times[m] = ACT_365.getDayCountFraction(valuationDate, marketTenors[m - 1]);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS for calibration
    final LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    // Call the constructor to create a calibrate hazard rate curve object
    final CalibrateHazardRateCurveLegacyCreditDefaultSwap hazardRateCurve = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calibrate the hazard rate curve to the market observed par CDS spreads (returns calibrated hazard rates as a vector of doubles)
    //final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, marketTenors, spreads, yieldCurve, priceType);

    // ********************************** REMEMBER THIS i.e. PriceType == CLEAN for the standard ISDA model *****************************************************************************
    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, marketTenors, marketSpreads, yieldCurve, PriceType.CLEAN);

    /*
    for (int m = 0; m < calibratedHazardRates.length; m++) {
      System.out.println(calibratedHazardRates[m]);
    }
    */

    final double[] modifiedHazardRateCurve = new double[calibratedHazardRates.length + 1];

    modifiedHazardRateCurve[0] = calibratedHazardRates[0];

    for (int m = 1; m < modifiedHazardRateCurve.length; m++) {
      modifiedHazardRateCurve[m] = calibratedHazardRates[m - 1];
    }

    // Build a hazard rate curve object based on the input market data
    return new HazardRateCurve(marketTenors, times, modifiedHazardRateCurve, 0.0);
  }
}
