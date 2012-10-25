/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.hazardratemodel;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to calibrate a single-name CDS hazard rate term structure to market observed term structure of par CDS spreads
 * The input is a vector of tenors and market observed par CDS spread quotes for those tenors
 * The output is a vector of calibrated term structure of hazard rates for those tenors
 */
public class CalibrateHazardRateCurve {

  // ------------------------------------------------------------------------

  //private static DayCount s_act365 = new ActualThreeSixtyFive();

  // Set the maximum number of iterations, tolerance and range of the hazard rate bounds for the root finder

  private static final int DEFAULT_MAX_NUMBER_OF_ITERATIONS = 100;
  private final int _maximumNumberOfIterations;

  private static final double DEFAULT_TOLERANCE = 1e-15;
  private final double _tolerance;

  private static final double DEFAULT_HAZARD_RATE_RANGE_MULTIPLIER = 0.5;
  private final double _hazardRateRangeMultiplier;

  // ------------------------------------------------------------------------

  // Ctor to initialise a CalibrateSurvivalCurve object with the default values for the root finder
  public CalibrateHazardRateCurve() {
    this(DEFAULT_MAX_NUMBER_OF_ITERATIONS, DEFAULT_TOLERANCE, DEFAULT_HAZARD_RATE_RANGE_MULTIPLIER);
  }

  // Ctor to initialise a CalibrateSurvivalCurve object with user specified values for the root finder
  public CalibrateHazardRateCurve(int maximumNumberOfIterations, double tolerance, double hazardRateRangeMultiplier) {
    _tolerance = tolerance;
    _maximumNumberOfIterations = maximumNumberOfIterations;
    _hazardRateRangeMultiplier = hazardRateRangeMultiplier;
  }

  // ------------------------------------------------------------------------

  // TODO : Lots of work to do in here still - Work In Progress

  // TODO : Replace the root finder with something more sophisticated (bisection was used to ensure a root is found if it exists)
  // TODO : Add a method to convert the hazard rates to survival probabilities
  // TODO : Not happy with the structure of this solution (would prefer to input and return a DoublesCurve object not a single vector) - need to revisit

  // ------------------------------------------------------------------------

  // Member function to calibrate a CDS objects hazard rate term structure to a term structure of market observed par CDS spreads
  // The input CDS object has all the schedule etc settings for computing the CDS's PV's etc
  // The user inputs the schedule of (future) dates on which we have observed par CDS spread quotes

  public double[] getCalibratedHazardRateTermStructure(LegacyCreditDefaultSwapDefinition cds, ZonedDateTime[] tenors, double[] marketSpreads, ISDACurve yieldCurve) {

    // ----------------------------------------------------------------------------

    // Check the input arguments

    // Check input CDS and YieldCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");

    // Check user data input is not null
    ArgumentChecker.notNull(tenors, "Tenors field");
    ArgumentChecker.notNull(marketSpreads, "Market observed CDS spreads field");

    // Check that the number of input tenors matches the number of input spreads
    ArgumentChecker.isTrue(tenors.length == marketSpreads.length, "Number of tenors and number of spreads should be equal");

    // Check the efficacy of the input market data
    for (int m = 0; m < tenors.length; m++) {

      ArgumentChecker.isTrue(tenors[m].isAfter(cds.getValuationDate()), "Calibration instrument of tenor {} is before the valuation date {}", tenors[m], cds.getValuationDate());

      if (tenors.length > 1 && m > 0) {
        ArgumentChecker.isTrue(tenors[m].isAfter(tenors[m - 1]), "Tenors not in ascending order");
      }

      ArgumentChecker.notNegative(marketSpreads[m], "Market spread at tenor " + tenors[m]);
      ArgumentChecker.notZero(marketSpreads[m], _tolerance, "Market spread at tenor " + tenors[m]);
    }

    // ----------------------------------------------------------------------------

    // Vector of (calibrated) piecewise constant hazard rates that we compute from the solver
    double[] hazardRates = new double[tenors.length];

    // ----------------------------------------------------------------------------

    // Build a cashflow schedule object - need to do this just to convert tenors to doubles (bit wasteful)
    GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Convert the ZonedDateTime tenors into doubles (measured from valuationDate)
    double[] tenorsAsDoubles = cashflowSchedule.convertTenorsToDoubles(tenors, cds.getValuationDate(), DayCountFactory.INSTANCE.getDayCount("ACT/365"));

    // Create an object for getting the PV of a CDS
    final PresentValueLegacyCreditDefaultSwap presentValueCDS = new PresentValueLegacyCreditDefaultSwap();

    // Create a calibration CDS object from the input CDS (maturity and contractual spread of this CDS will vary as we bootstrap up the hazard rate term structure)
    LegacyCreditDefaultSwapDefinition calibrationCDS = cds;

    // ----------------------------------------------------------------------------

    // Loop through each of the input tenors
    for (int m = 0; m < tenors.length; m++) {

      // Construct a temporary vector of the first m tenors (note size of array)
      double[] runningTenors = new double[m + 1];

      double[] runningHazardRates = new double[m + 1];

      // Populate this vector with the first m tenors (needed to construct the survival curve using these tenors)
      for (int i = 0; i <= m; i++) {
        runningTenors[i] = tenorsAsDoubles[i];
        runningHazardRates[i] = hazardRates[i];
      }

      // Modify the calibration CDS to have a maturity of tenor[m] 
      calibrationCDS = calibrationCDS.withMaturity(tenors[m]);

      // Modify the calibration CDS to have a contractual spread of marketSpread[m]
      calibrationCDS = calibrationCDS.withSpread(marketSpreads[m]);

      // Compute the calibrated hazard rate for tenor[m] (using the calibrated hazard rates for tenors 1, ..., m - 1) 
      hazardRates[m] = calibrateHazardRate(calibrationCDS, presentValueCDS, yieldCurve, runningTenors, runningHazardRates);
    }

    // ----------------------------------------------------------------------------

    return hazardRates;
  }

  // ------------------------------------------------------------------------

  // Private method to do the root search to find the hazard rate for tenor m which gives the CDS a PV of zero
  private double calibrateHazardRate(
      LegacyCreditDefaultSwapDefinition calibrationCDS,
      PresentValueLegacyCreditDefaultSwap presentValueCDS,
      ISDACurve yieldCurve,
      double[] runningTenors,
      double[] hazardRates) {

    // ------------------------------------------------------------------------

    double deltaHazardRate = 0.0;
    double calibratedHazardRate = 0.0;

    // ------------------------------------------------------------------------

    // Calculate the initial guess for the calibrated hazard rate for this tenor
    double hazardRateGuess = (calibrationCDS.getParSpread() / 10000.0) / (1 - calibrationCDS.getRecoveryRate());

    // Calculate the initial bounds for the hazard rate search
    double lowerHazardRate = (1.0 - _hazardRateRangeMultiplier) * hazardRateGuess;
    double upperHazardRate = (1.0 + _hazardRateRangeMultiplier) * hazardRateGuess;

    // ------------------------------------------------------------------------

    // Make sure the initial hazard rate bounds are in the range [0, 1] (otherwise would have arbitrage)
    if (lowerHazardRate < 0.0) {
      lowerHazardRate = 0.0;
    }

    if (upperHazardRate > 1.0) {
      upperHazardRate = 1.0;
    }

    // ------------------------------------------------------------------------

    // Construct a hazard rate term structure curve using the (calibrated) first m tenors in runningTenors
    HazardRateCurve hazardRateCurve = new HazardRateCurve(runningTenors, hazardRates, 0.0);

    // ------------------------------------------------------------------------

    // Now do the root search (in hazard rate space) - simple bisection method for the moment (guaranteed to work and we are not concerned with speed at the moment)

    // Calculate the CDS PV at the lower hazard rate bound
    double cdsPresentValueAtLowerPoint = calculateCDSPV(calibrationCDS, presentValueCDS, runningTenors, hazardRates, lowerHazardRate, yieldCurve, hazardRateCurve);

    // Calculate the CDS PV at the upper hazard rate bound
    double cdsPresentValueAtMidPoint = calculateCDSPV(calibrationCDS, presentValueCDS, runningTenors, hazardRates, upperHazardRate, yieldCurve, hazardRateCurve);

    // Orient the search
    if (cdsPresentValueAtLowerPoint < 0.0) {

      deltaHazardRate = upperHazardRate - lowerHazardRate;
      calibratedHazardRate = lowerHazardRate;

    } else {

      deltaHazardRate = lowerHazardRate - upperHazardRate;
      calibratedHazardRate = upperHazardRate;

    }

    // The actual bisection routine
    for (int i = 0; i < _maximumNumberOfIterations; i++) {

      // Cut the hazard rate range in half
      deltaHazardRate = deltaHazardRate * 0.5;

      // Calculate the new mid-point
      double hazardRateMidpoint = calibratedHazardRate + deltaHazardRate;

      // Calculate the CDS PV at the hazard rate range midpoint
      cdsPresentValueAtMidPoint = calculateCDSPV(calibrationCDS, presentValueCDS, runningTenors, hazardRates, hazardRateMidpoint, yieldCurve, hazardRateCurve);

      if (Double.doubleToLongBits(cdsPresentValueAtMidPoint) <= 0.0) {
        calibratedHazardRate = hazardRateMidpoint;
      }

      // Check to see if we have converged to within the specified tolerance or that we are at the root
      if (Math.abs(deltaHazardRate) < _tolerance || Double.doubleToLongBits(cdsPresentValueAtMidPoint) == 0.0) {
        return calibratedHazardRate;
      }
    }

    // ------------------------------------------------------------------------

    return 0.0;
  }

  // ------------------------------------------------------------------------

  // Private member function to compute the PV of a CDS given a particular guess for the hazard rate at tenor m (given calibrated hazard rates for tenors 0, ..., m - 1)
  private double calculateCDSPV(LegacyCreditDefaultSwapDefinition calibrationCDS,
      PresentValueLegacyCreditDefaultSwap presentValueCDS,
      double[] tenors,
      double[] hazardRates,
      double hazardRateMidPoint,
      ISDACurve yieldCurve,
      HazardRateCurve hazardRateCurve) {

    // How many tenors in the hazard rate term structure have been previously calibrated
    int numberOfTenors = tenors.length;

    // Put the hazard rate guess into the vector of hazard rates as the last element in the array
    hazardRates[numberOfTenors - 1] = hazardRateMidPoint;

    // Modify the survival curve so that it has the modified vector of hazard rates as an input to the ctor
    hazardRateCurve = hazardRateCurve.bootstrapHelperHazardRateCurve(tenors, hazardRates);

    // Compute the PV of the CDS with this term structure of hazard rates
    double cdsPresentValueAtMidpoint = presentValueCDS.getPresentValueCreditDefaultSwap(calibrationCDS, yieldCurve, hazardRateCurve);

    return cdsPresentValueAtMidpoint;
  }

  // ------------------------------------------------------------------------
}
