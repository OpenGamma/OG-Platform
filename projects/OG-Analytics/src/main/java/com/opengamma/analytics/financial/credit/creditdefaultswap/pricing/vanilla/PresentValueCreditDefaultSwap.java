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

  private static final int cashSettlementDays = 3;

  private static final boolean businessDayAdjustCashSettlementDate = true;

  private static final BusinessDayConvention cashSettlementDateBusinessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("F");

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  private static final double spreadLowerBound = 1e-10;
  private static final double spreadUpperBound = 1e10;

  // Create objects used for the construction of the various legs in the CDS valuation
  private static final GenerateCreditDefaultSwapPremiumLegSchedule premiumLegScheduleBuilder = new GenerateCreditDefaultSwapPremiumLegSchedule();
  private static final GenerateCreditDefaultSwapIntegrationSchedule contingentLegScheduleBuilder = new GenerateCreditDefaultSwapIntegrationSchedule();
  private static final GenerateCreditDefaultSwapIntegrationSchedule accruedLegScheduleBuilder = new GenerateCreditDefaultSwapIntegrationSchedule();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : The code in this class is a complete mess at the moment - will be completely rewritten and modularised 

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Will need to revisit to look at how we can optimise the calculations (e.g. not repeating schedule generation calculations)

  // TODO : Need to add the PROT_PAY_MAT option as well

  // TODO : when the ISDA calibration routine fails, then should fall back to the simple bi-section that was originally implemented
  // TODO : since this routine very rarely falls over

  // TODO : Need to move the calibration code out of this class

  // TODO : Check if valDate > matDate and return zero if so in the contingent leg calculation
  // TODO : Remember that the start date for protection to begin (in the contingent leg calculation) is MAX(stepinDate, startDate)

  // TODO : Need to move the calculateWorkDays function into the schedule generation class (and check the logic more thoroughly)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS (with a hazard rate curve calibrated to market observed data)

  @Deprecated
  public double calculatePremiumLeg(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double presentValuePremiumLeg = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Build the premium leg cashflow schedule from the contract specification
    final ZonedDateTime[][] premiumLegSchedule = premiumLegScheduleBuilder.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);

    // Build the integration schedule for the calculation of the accrued leg (this is not a cashflow schedule per se, but a set of time nodes for evaluating the accrued payment integral)
    final ZonedDateTime[] accruedLegIntegrationSchedule = accruedLegScheduleBuilder.constructCreditDefaultSwapAccruedLegIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, false);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double thisPV = 0.0;

    int obsOffset = 0;

    final ZonedDateTime today = valuationDate;
    final ZonedDateTime stepinDate = cds.getEffectiveDate();

    // TODO : Add the extra logic for this calculation (safe for the moment since 'protectionStart' is TRUE always)
    // TODO : ISDA uses accEndDates - check this
    //final ZonedDateTime matDate = cds.getMaturityDate();
    final ZonedDateTime matDate = premiumLegSchedule[premiumLegSchedule.length - 1][2].minusDays(1);

    // TODO : Check valueDate >= today and stepinDate >= today

    if (today.isAfter(matDate) || stepinDate.isAfter(matDate)) {

      // Trade has no remaining value
      presentValuePremiumLeg = 0.0;

      return presentValuePremiumLeg;
    }

    if (cds.getProtectionStart()) {
      obsOffset = -1;
    }

    // ---------------------------

    // Note the start index of this loop
    for (int i = 1; i < premiumLegSchedule.length; i++) {

      // ---------------------------

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

      // Compensate Java shortcoming
      if (Double.compare(tObsOffset, -0.0) == 0) {
        tObsOffset = 0;
      }
      // Calculate the survival probability and discount factor
      final double survival = hazardRateCurve.getSurvivalProbability(tObsOffset);
      final double discount = yieldCurve.getDiscountFactor(t);

      // Compute the contribution of this accrual period to the overall fee leg value
      thisPV += accTime * discount * survival;

      // ---------------------------------------------

      double myPV = 0.0;

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

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double valueDatePV = calculateCashSettlementDiscountFactor(cds, valuationDate, cashSettlementDays, cashSettlementDateBusinessDayConvention, yieldCurve);

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

  private double calculateAccruedInterest(final CreditDefaultSwapDefinition cds, final ZonedDateTime[][] premiumLegSchedule, final ZonedDateTime stepinDate) {

    // TODO : Maybe check if stepinDate is in range [startDate, maturityDate + 1] - probably not necessary since the valuation will not allow this
    // TODO : Check we are using the correct column of the matrix below - may be the cause of a difference

    // Start at the beginning of the cashflow schedule
    //ZonedDateTime rollingDate = premiumLegSchedule[0];
    ZonedDateTime rollingDate = premiumLegSchedule[0][0];
    //ZonedDateTime rollingDate = premiumLegSchedule[1][1];

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

    final double ai = cds.getDayCountFractionConvention().getDayCountFraction(previousPeriod, stepinDate);

    return ai;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Function to compute the discount factor for discounting the upfront payment made at the cash settlement date back to the valuation date

  private double calculateCashSettlementDiscountFactor(
      final CreditDefaultSwapDefinition cds,
      final ZonedDateTime valuationDate,
      final int spotDays,
      final BusinessDayConvention cashSettlementDateBusinessDayConvention,
      final ISDADateCurve yieldCurve) {

    // From the valuationDate, determine the next working day spotDays in the future
    ZonedDateTime bdaCashSettlementDate = calculateWorkdays(cds, valuationDate, spotDays);

    // Compute the time between the valuationDate and the business day adjusted cash settlement date
    final double timeToCashSettlement = TimeCalculator.getTimeBetween(valuationDate, bdaCashSettlementDate);

    // Compute the discount factor
    final double cashSettlementDateDiscountFactor = yieldCurve.getDiscountFactor(timeToCashSettlement);

    return cashSettlementDateDiscountFactor;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private ZonedDateTime calculateWorkdays(
      final CreditDefaultSwapDefinition cds,
      final ZonedDateTime valuationDate,
      final int spotDays) {

    ArgumentChecker.notNegative(spotDays, "Cash settlement days");

    ZonedDateTime requiredDate = valuationDate;

    if (spotDays > 0) {
      int n = 0;

      for (int i = 0; i < spotDays; i++) {

        requiredDate = requiredDate.plusDays(1);

        if (!cds.getCalendar().isWorkingDay(requiredDate.toLocalDate())) {
          n++;
        }
      }

      requiredDate = requiredDate.plusDays(n);

      while (!cds.getCalendar().isWorkingDay(requiredDate.toLocalDate())) {
        requiredDate = requiredDate.plusDays(1);
      }
    }

    return requiredDate;
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

    if (cds.getProtectionStart()) {
      clStartDate = valuationDate.minusDays(1);
    }

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

    // Build the integration schedule for the calculation of the contingent leg (this is not a cashflow schedule per se, but a set of time nodes for evaluating the contingent leg integral)
    final double[] contingentLegIntegrationSchedule = contingentLegScheduleBuilder.constructCreditDefaultSwapContingentLegIntegrationSchedule(
        valuationDate,
        startDate,
        clEndDate,
        cds,
        yieldCurve,
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
      presentValueContingentLeg += lossGivenDefault * (hazardRate / (hazardRate + interestRate)) * (1.0 - Math.exp(-(hazardRate + interestRate) * deltat)) * survivalProbabilityPrevious *
          discountFactorPrevious;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the discount factor discounting the upfront payment made on the cash settlement date back to the valuation date
    final double valueDatePV = calculateCashSettlementDiscountFactor(cds, valuationDate, cashSettlementDays, cashSettlementDateBusinessDayConvention, yieldCurve);

    return cds.getNotional() * presentValueContingentLeg / valueDatePV;
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

    final double presentValuePremiumLeg = (cds.getParSpread() / 10000) * calculatePremiumLeg(valuationDate, cds, yieldCurve, hazardRateCurve, PriceType.CLEAN) / cds.getNotional();
    final double presentValueContingentLeg = calculateContingentLeg(valuationDate, cds, yieldCurve, hazardRateCurve) / cds.getNotional();

    final double presentValue = presentValueContingentLeg - presentValuePremiumLeg;

    return presentValue;
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

    // TODO : Need to make the routine fail more elegantly than this - don't want to fail and return nonsense
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

  // The ISDA calibration routine (this is the equivalent of the 'CdsBootstrap' function in the ISDA code)

  public HazardRateCurve newCalibrateHazardRateCurve(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve) {

    // ------------------------------

    // TODO : Check the input arguments (not null, market data compatible etc)

    // ------------------------------

    // Create a CDS whose maturity and spreads we will vary to be that of the calibration instruments
    LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;

    // This vector will store the bootstrapped hazard rates that will be used to construct the calibrated hazard rate term structure object
    double[] calibratedHazardRateCurve = new double[marketTenors.length];

    // The tenorsAsDoubles vector includes time zero (valuationDate)
    final double[] tenorsAsDoubles = new double[marketTenors.length + 1];

    tenorsAsDoubles[0] = 0.0;
    for (int m = 1; m <= marketTenors.length; m++) {
      tenorsAsDoubles[m] = ACT_365.getDayCountFraction(valuationDate, marketTenors[m - 1]);
    }

    // ------------------------------

    // Loop over each of the calibration instruments
    for (int i = 0; i < marketTenors.length; i++) {

      // Remember that the input spreads are in bps, therefore need dividing by 10,000
      final double guess = (marketSpreads[i] / 10000.0) / (1 - cds.getRecoveryRate());

      // Modify the input CDS to have the maturity of the current calibration instrument
      calibrationCDS = calibrationCDS.withMaturityDate(marketTenors[i]);

      // Modify the input CDS to have the par spread of the current calibration instrument
      calibrationCDS = calibrationCDS.withSpread(marketSpreads[i]);

      // Now need to build a HazardRateCurve object from the first i calibrated points
      double[] runningTenorsAsDoubles = new double[i + 1];
      double[] runningHazardRates = new double[i + 1];

      ZonedDateTime[] runningMarketTenors = new ZonedDateTime[i + 1];

      // Set the hazard rate for the current calibration instrument to be the initial 'guess'
      calibratedHazardRateCurve[i] = guess;

      // Set up the inputs for the hazard rate curve construction
      for (int m = 0; m <= i; m++) {
        runningMarketTenors[m] = marketTenors[m];
        runningTenorsAsDoubles[m] = ACT_365.getDayCountFraction(valuationDate, runningMarketTenors[m]);
        runningHazardRates[m] = calibratedHazardRateCurve[m];
      }

      // Now build a (running) hazard rate curve for the first i tenors where the hazard rate for tenor i is 'guess'
      HazardRateCurve runningHazardRateCurve = new HazardRateCurve(runningMarketTenors, runningTenorsAsDoubles, runningHazardRates, 0.0);

      // Now calculate the calibrated hazard rate for tenor i (given that the prior tenors have been calibrated) using the ISDA calibration routine
      calibratedHazardRateCurve[i] = jpmCDSRootFindBrent(valuationDate, calibrationCDS, yieldCurve, runningHazardRateCurve, guess, PriceType.CLEAN);
    }

    // ------------------------------

    final double[] modifiedHazardRateCurve = new double[calibratedHazardRateCurve.length + 1];

    modifiedHazardRateCurve[0] = calibratedHazardRateCurve[0];

    for (int m = 1; m < modifiedHazardRateCurve.length; m++) {
      modifiedHazardRateCurve[m] = calibratedHazardRateCurve[m - 1];
    }

    // Now build the complete, calibrated hazard rate curve
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

    final double[] modifiedHazardRateCurve = new double[calibratedHazardRates.length + 1];

    modifiedHazardRateCurve[0] = calibratedHazardRates[0];

    for (int m = 1; m < modifiedHazardRateCurve.length; m++) {
      modifiedHazardRateCurve[m] = calibratedHazardRates[m - 1];
    }

    // Build a hazard rate curve object based on the input market data
    return new HazardRateCurve(marketTenors, times, modifiedHazardRateCurve, 0.0);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
