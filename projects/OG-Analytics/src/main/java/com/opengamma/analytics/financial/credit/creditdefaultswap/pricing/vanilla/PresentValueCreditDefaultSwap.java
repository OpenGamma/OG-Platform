/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy.CalibrateHazardRateCurveLegacyCreditDefaultSwap;
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

  private static final int spotDays = 3;

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

  // TODO : Add the calculation of the cash settlement amount

  // ----------------------------------------------------------------------------------------------------------------------------------------

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

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS (with a hazard rate curve calibrated to market observed data)

  // The code for the accrued calc has just been lifted from RiskCare's implementation for now because it exactly reproduces
  // the ISDA model - will replace with a better model in due course

  public double calculatePremiumLeg(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Local variable definitions
    final int startIndex = 0;

    double presentValuePremiumLeg = 0.0;
    final double presentValueAccruedInterest = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct a cashflow schedule object for the premium leg
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    //final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    final ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);

    // Construct a schedule object for the accrued leg (this is not a cashflow schedule per se, but a set of time nodes for evaluating the accrued payment integral)
    final GenerateCreditDefaultSwapIntegrationSchedule accruedSchedule = new GenerateCreditDefaultSwapIntegrationSchedule();

    // Build the integration schedule for the calculation of the accrued leg
    final ZonedDateTime[] accruedLegIntegrationSchedule = accruedSchedule.constructCreditDefaultSwapAccruedLegIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, false);

    // Calculate the stepin time with the appropriate offset
    final double offsetStepinTime = accruedSchedule.calculateCreditDefaultSwapOffsetStepinTime(valuationDate, cds, ACT_365);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Get the (adjusted) maturity date of the trade
    final ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);

    final ZonedDateTime startDate = premiumLegSchedule[0];

    // ----------------------------------------------------------------------------------------------------------------------------------------

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
    final ZonedDateTime stepinDate = cds.getEffectiveDate(); //TODO this relies on the person that's set up the CDS to know that effective date = 1 day after valuation date by convention

    // The value date is when cash settlement is made
    final ZonedDateTime valueDate = valuationDate;

    // TODO : Add the extra logic for this calculation
    final ZonedDateTime matDate = cds.getMaturityDate();

    // TODO : Check valueDate >= today and stepinDate >= today

    // TODO : Check when today > matDate || stepinDate > matDate
    if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {
      presentValuePremiumLeg = 0.0;
      return presentValuePremiumLeg;
    }

    double thisPV = 0.0;

    for (int i = 1; i < premiumLegSchedule.length; i++) {

      int obsOffset = 0;

      if (cds.getProtectionStart()) {
        obsOffset = -1;
      }

      final ZonedDateTime accrualStartDate = premiumLegSchedule[i - 1];
      ZonedDateTime accrualEndDate = premiumLegSchedule[i];

      // The last coupon date has an extra day of accrued
      if (i == premiumLegSchedule.length - 1) {
        accrualEndDate = accrualEndDate.plusDays(1);
      }

      double delta = 1.0;

      final boolean temp = accrualEndDate.isAfter(stepinDate);

      // TODO : Check accEndDate <= stepinDate
      if (temp == false) {
        delta = 0.0;
      }

      final double accTime = TimeCalculator.getTimeBetween(accrualStartDate, accrualEndDate, ACT_360);

      ZonedDateTime discountDate = accrualEndDate;

      if (i == premiumLegSchedule.length - 1) {

        //obsOffset = 0;

        discountDate = accrualEndDate.minusDays(1);

        //accTime = TimeCalculator.getTimeBetween(accrualStartDate, accrualEndDate.plusDays(1), ACT_360);
      }

      double tObsOffset = TimeCalculator.getTimeBetween(today, accrualEndDate.plusDays(obsOffset), ACT_365);
      if (Double.compare(tObsOffset, -0.0) == 0) {
        tObsOffset = 0;
      }

      //double t = TimeCalculator.getTimeBetween(today, accrualEndDate, ACT_365);

      double t = TimeCalculator.getTimeBetween(today, discountDate, ACT_365);

      final double survival = hazardRateCurve.getSurvivalProbability(tObsOffset);
      final double discount = yieldCurve.getDiscountFactor(t);

      //final double discount = yieldCurve.getDiscountFactor(today, accrualEndDate);

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
      }

      // ---------------------------------------------

      thisPV += myPV;
    }

    presentValuePremiumLeg = thisPV;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Check this calculation - maybe move it out of this routine and into the PV calculation routine?
    // TODO : Note the cash settlement date is hardcoded at 3 days

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

      // pass in stepinDate as 'today' 31/1/2013

      double ai = 0.0;

      // TODO : Maybe check if stepinDate is in range [startDate, maturityDate] - probably not necessary

      //final int startCashflowIndex = getCashflowIndex(stepinDate, premiumLegSchedule, 0, 1);

      int startCashflowIndex = 0;

      ZonedDateTime rollingDate = premiumLegSchedule[0].minusDays(1);

      while (rollingDate.isBefore(premiumLegSchedule[startCashflowIndex])) {
        startCashflowIndex++;
        rollingDate = premiumLegSchedule[startCashflowIndex];
      }

      // Get the date of the last coupon before the current valuation date
      final ZonedDateTime previousPeriod = premiumLegSchedule[startCashflowIndex - 1];

      // Compute the amount of time between previousPeriod and stepinDate
      final double dcf = cds.getDayCountFractionConvention().getDayCountFraction(previousPeriod, stepinDate);

      // Calculate the accrued interest gained in this period of time
      ai = /*(cds.getParSpread() / 10000.0) * */dcf;

      presentValuePremiumLeg -= ai;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    /*
    // Calculate the value of the remaining premium and accrual payments (due after valuationDate)
    for (int i = startCashflowIndex; i < premiumLegSchedule.length; i++) {

      // Get the beginning and end dates of the current coupon
      ZonedDateTime accrualStart = premiumLegSchedule[i - 1];
      ZonedDateTime accrualEnd = premiumLegSchedule[i];

      //ZonedDateTime offsetAccStartDate = accrualStartDate.plusDays(obsOffset);
      //ZonedDateTime offsetAccEndDate = accrualEndDate.plusDays(obsOffset);

      //final double startTime = TimeCalculator.getTimeBetween(valuationDate, offsetAccStartDate, ACT_360);
      //final double endTime = TimeCalculator.getTimeBetween(valuationDate, offsetAccEndDate, ACT_360);

      //final double[] truncatedTimeline = accruedSchedule.getTruncatedTimeLine(accruedLegIntegrationSchedule, startTime, endTime);

      // ----------------------------------------------------------------------------------------------------------------------------------------

      // Calculate the time between the valuation date (time at which survival probability is unity) and the current cashflow
      double t = TimeCalculator.getTimeBetween(valuationDate, accrualEnd, ACT_365);

      // Calculate the discount factor at time t
      final double discountFactor = yieldCurve.getDiscountFactor(t);

      // ----------------------------------------------------------------------------------------------------------------------------------------

      // If protection starts at the beginning of the period ...
      if (cds.getProtectionStart()) {

        // ... Roll all but the last date back by 1/365 of a year
        if (i < premiumLegSchedule.length - 1) {
          t -= cds.getProtectionOffset();
        }

        // This is a bit of a hack - need a more elegant way of dealing with the timing nuances
        if (i == 1) {
          accrualStart = accrualStart.minusDays(1);
        }

        // ... Roll the final maturity date forward by one day
        if (i == premiumLegSchedule.length - 1) {
          accrualEnd = accrualEnd.plusDays(1);
        }
      }

      // ----------------------------------------------------------------------------------------------------------------------------------------

      // Compute the daycount fraction for the current accrual period
      final double dcf = cds.getDayCountFractionConvention().getDayCountFraction(accrualStart, accrualEnd);

      // Calculate the survival probability at the modified time t
      final double survivalProbability = hazardRateCurve.getSurvivalProbability(t);

      // Add this discounted cashflow to the running total for the value of the premium leg
      //presentValuePremiumLeg += dcf * discountFactor * survivalProbability;

      // ----------------------------------------------------------------------------------------------------------------------------------------

      // Now calculate the accrued leg component if required (need to re-write this code)

      if (cds.getIncludeAccruedPremium()) {
        final double stepinDiscountFactor = 1.0;
        int endIndex;

        Arrays.sort(accruedLegIntegrationSchedule); //TODO is this extra sorting necessary?

        for (endIndex = startIndex; endIndex < accruedLegIntegrationSchedule.length; endIndex++) {
          if (accruedLegIntegrationSchedule[endIndex] >= t) {
            break;
          }
        }
        if (endIndex >= accruedLegIntegrationSchedule.length) {
          endIndex = accruedLegIntegrationSchedule.length - 1;
        }
        presentValueAccruedInterest += valueFeeLegAccrualOnDefault(dcf, accruedLegIntegrationSchedule, yieldCurve, hazardRateCurve, startIndex, endIndex, offsetStepinTime, stepinDiscountFactor);

      }

      // ----------------------------------------------------------------------------------------------------------------------------------------

    }
     */

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

  public double calculateContingentLeg(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    /*
     * if protStart offset = 1 else 0
     * 
     * startDate = MAX(cl->startDate(150509), stapinDate(150510) - offset)
     * startDate = MAX(startDate, today(150509) - offset)
     * 
     * case PROT_PAY_DEF goto onePeriodIntegral
     *    tl = JpmcdsRiskyTimeline
     * 
     * 
     */

    // TODO : Check if valDate > matDate and return zero if so

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : Remember that the start date for protection to begin is MAX(today, startDate)

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

    Function1D<Double, Double> function = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double parSpread) {

        // For this value of the flat spread, compute the upfront amount
        marketSpreads[0] = parSpread;
        final double pointsUpfront = calculateUpfrontFlat(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

        // Compute the difference between the calculated and input upfront amount 
        final double delta = pointsUpfront - upfrontAmount;

        return delta;
      }
    };

    double parSpreadFlat = new BisectionSingleRootFinder().getRoot(function, spreadLowerBound, spreadUpperBound);

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

  @Deprecated
  public HazardRateCurve calibrateHazardRateCurve(final ZonedDateTime valuationDate,
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

    // ********************************** REMEMBER THIS **********************************************************************************************
    final double[] calibratedHazardRates = hazardRateCurve.getCalibratedHazardRateTermStructure(valuationDate, calibrationCDS, marketTenors, marketSpreads, yieldCurve, PriceType.CLEAN);

    final double[] modifiedHazardRateCurve = new double[calibratedHazardRates.length + 1];

    modifiedHazardRateCurve[0] = calibratedHazardRates[0];

    for (int m = 1; m < modifiedHazardRateCurve.length; m++) {
      modifiedHazardRateCurve[m] = calibratedHazardRates[m - 1];
    }

    // Build a hazard rate curve object based on the input market data
    return new HazardRateCurve(marketTenors, times, modifiedHazardRateCurve, 0.0);
  }
}
