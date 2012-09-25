/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to calibrate a single-name CDS survival curve to market observed term structure of par CDS spreads
 * The input is a vector of tenors and market observed par CDS spread quotes for those tenors
 * The output is a vector of tenors (represented as doubles) and the calibrated survival probabilities for those tenors
 */
public class CalibrateSurvivalCurve {

  // ------------------------------------------------------------------------

  // Set the maximum number of iterations, tolerance and range of the hazard rate bounds for the root finder

  private static final int DEFAULT_MAX_NUMBER_OF_ITERATIONS = 40;
  private final int _maximumNumberOfIterations;

  private static final double DEFAULT_TOLERANCE = 1e-10;
  private final double _tolerance;

  private static final double DEFAULT_HAZARD_RATE_RANGE_MULTIPLIER = 0.5;
  private final double _hazardRateRangeMultiplier;

  // Ctor to initialise a CalibrateSurvivalCurve object with the default values for the root finder
  public CalibrateSurvivalCurve() {
    this(DEFAULT_MAX_NUMBER_OF_ITERATIONS, DEFAULT_TOLERANCE, DEFAULT_HAZARD_RATE_RANGE_MULTIPLIER);
  }

  //Ctor to initialise a CalibrateSurvivalCurve object with user specified values for the root finder
  public CalibrateSurvivalCurve(int maximumNumberOfIterations, double tolerance, double hazardRateRangeMultiplier) {
    _tolerance = tolerance;
    _maximumNumberOfIterations = maximumNumberOfIterations;
    _hazardRateRangeMultiplier = hazardRateRangeMultiplier;
  }

  // ------------------------------------------------------------------------

  // TODO : Lots of work to do in here still a complete mess at the moment - Work In Progress

  // TODO : Add arg checkers to check that length of the tenor and parCDSSpreads vectors are the same
  // TODO : Replace the root finder with something more sophisticated (bisection was used to ensure a root is found if it exists)
  // TODO : Verify that the valuation date is valid i.e. should be equal to the start date (or effective date - check this)
  // TODO : Should convertDatesToDoubles be moved into the schedule generation class (seems a more natural place for it)
  // TODO : Check the case where the spread is flat but very large e.g. 50K bps
  // TODO : Remember to make sure return survival probs NOT hazard rates

  // ------------------------------------------------------------------------

  // Member function to calibrate a CDS objects survival curve to a term structure of market observed par CDS spreads
  // The input CDS object has all the schedule etc settings for computing the CDS's PV's etc
  // The user inputs the schedule of (future) dates on which we have observed par CDS spread quotes

  public double[] getCalibratedSurvivalCurve(CreditDefaultSwapDefinition cds, ZonedDateTime[] tenors, double[] marketSpreads, YieldCurve yieldCurve) {

    // ----------------------------------------------------------------------------

    int numberOfTenors = tenors.length;

    // ----------------------------------------------------------------------------

    // Check the input arguments

    // Check input CDS and YieldCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");

    // Check user data input is not null
    ArgumentChecker.notNull(tenors, "Tenors field");
    ArgumentChecker.notNull(marketSpreads, "Market observed CDS spreads field");

    for (int m = 1; m < numberOfTenors; m++) {
      ArgumentChecker.isTrue(tenors[m].isAfter(tenors[m - 1]), "Tenors not in ascending order");
      ArgumentChecker.notNegative(marketSpreads[m], "Market spread at tenor " + tenors[m]);
      ArgumentChecker.notZero(marketSpreads[m], _tolerance, "Market spread at tenor " + tenors[m]);
    }

    // ----------------------------------------------------------------------------

    // Get the valuation date of the CDS
    ZonedDateTime valuationDate = cds.getValuationDate();

    // Get the daycount fraction convention
    DayCount dayCount = cds.getDayCountFractionConvention();

    // ----------------------------------------------------------------------------

    // Vector of (calibrated) piecewise constant hazard rates that we compute from the solver
    double[] hazardRates = new double[numberOfTenors];

    // Vector of survival probabilities that are to be returned (does not include time zero - valuationDate)
    double[] calibratedSurvivalCurve = new double[numberOfTenors];

    // ----------------------------------------------------------------------------

    // Build a cashflow schedule - need to do this just to convert tenors to doubles - bit stupid having to do this
    GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Convert the ZonedDateTime tenors into doubles (measured from valuationDate)
    double[] tenorsAsDoubles = cashflowSchedule.convertDatesToDoubles(valuationDate, tenors, dayCount);

    // Create an object for getting the PV of a CDS
    final PresentValueCreditDefaultSwap presentValueCDS = new PresentValueCreditDefaultSwap();

    // Create a calibration CDS object from the input CDS (maturity and contractual spread of this CDS will vary as we bootstrap the survival curve)
    CreditDefaultSwapDefinition calibrationCDS = cds;

    // ----------------------------------------------------------------------------

    // Loop through each of the input tenors
    for (int m = 0; m < numberOfTenors; m++) {

      // Construct a temporary vector of the first m tenors
      double[] runningTenors = new double[m + 1];

      // Populate this vector with the first m tenors (needed to construct the survival curve using these tenors)
      for (int i = 0; i <= m; i++) {
        runningTenors[i] = tenorsAsDoubles[i];
      }

      // Modify the calibration CDS to have a maturity of tenor[m] and contractual spread marketSpread[m] 
      calibrationCDS = calibrationCDS.withMaturity(tenors[m]);
      calibrationCDS = calibrationCDS.withSpread(marketSpreads[m]);

      // Compute the calibrated hazard rate for tenor[m] (using the calibrated hazard rates for tenors 1, ..., m - 1) 
      hazardRates[m] = calibrateHazardRate(calibrationCDS, presentValueCDS, yieldCurve, runningTenors, hazardRates);

      // Just return the hazard rates for now - will replace with survival probabilities
      calibratedSurvivalCurve[m] = hazardRates[m];
    }

    // ----------------------------------------------------------------------------

    return calibratedSurvivalCurve;
  }

  // ------------------------------------------------------------------------

  // Private method to do the root search to find the hazard rate for tenor m which gives the CDS a PV of zero

  private double calibrateHazardRate(CreditDefaultSwapDefinition calibrationCDS,
      PresentValueCreditDefaultSwap presentValueCDS,
      YieldCurve yieldCurve,
      double[] runningTenors,
      double[] hazardRates) {

    // ------------------------------------------------------------------------

    double deltaHazardRate = 0.0;
    double calibratedHazardRate = 0.0;

    // Calculate the initial guess for the calibrated hazard rate
    double hazardRateGuess = (calibrationCDS.getPremiumLegCoupon() / 10000.0) / (1 - calibrationCDS.getCurveRecoveryRate());

    // Calculate the initial bounds for the hazard rate search
    double lowerHazardRate = (1.0 - _hazardRateRangeMultiplier) * hazardRateGuess;
    double upperHazardRate = (1.0 + _hazardRateRangeMultiplier) * hazardRateGuess;

    // Make sure the initial hazard rate bounds are in the range [0, 1] (otherwise would have arbitrage)
    if (lowerHazardRate < 0.0) {
      lowerHazardRate = 0.0;
    }

    if (upperHazardRate > 1.0) {
      upperHazardRate = 1.0;
    }

    // Construct a hazard rate curve using the first m tenors in runningTenors
    SurvivalCurve survivalCurve = new SurvivalCurve(runningTenors, hazardRates);

    // ------------------------------------------------------------------------

    // Now do the root search (in hazard rate space) - simple bisection method for the moment (guaranteed to work and we are not concerned with speed at the moment)

    // Calculate the CDS PV at the lower hazard rate bound
    double cdsPresentValueAtLowerPoint = calculateCDSPV(calibrationCDS, presentValueCDS, runningTenors, hazardRates, lowerHazardRate, yieldCurve, survivalCurve);

    // Calculate the CDS PV at the upper hazard rate bound
    double cdsPresentValueAtMidPoint = calculateCDSPV(calibrationCDS, presentValueCDS, runningTenors, hazardRates, upperHazardRate, yieldCurve, survivalCurve);

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
      cdsPresentValueAtMidPoint = calculateCDSPV(calibrationCDS, presentValueCDS, runningTenors, hazardRates, hazardRateMidpoint, yieldCurve, survivalCurve);

      if (cdsPresentValueAtMidPoint <= 0.0) {
        calibratedHazardRate = hazardRateMidpoint;
      }

      // Check to see if we have converged to within the specified tolerance or that we are at the root
      if (Math.abs(deltaHazardRate) < _tolerance || cdsPresentValueAtMidPoint == 0.0) {
        return calibratedHazardRate;
      }
    }

    // ------------------------------------------------------------------------

    return 0.0;
  }

  // ------------------------------------------------------------------------

  // Private member function to compute the PV of a CDS given a particular guess for the hazard rate at tenor m
  private double calculateCDSPV(CreditDefaultSwapDefinition calibrationCDS,
      PresentValueCreditDefaultSwap presentValueCDS,
      double[] tenors,
      double[] hazardRates,
      double hazardRateMidPoint,
      YieldCurve yieldCurve,
      SurvivalCurve survivalCurve) {

    int numberOfTenors = tenors.length;

    // Put the hazard rate guess into the vector of hazard rates as the last element in the array
    hazardRates[numberOfTenors - 1] = hazardRateMidPoint;

    // Modify the survival curve so that it has the modified vector of hazard rates as an input to the ctor
    survivalCurve = survivalCurve.bootstrapHelperSurvivalCurve(tenors, hazardRates);

    // Compute the PV of the CDS with this term structure of hazard rates
    double cdsPresentValueAtMidpoint = presentValueCDS.getPresentValueCreditDefaultSwap(calibrationCDS, yieldCurve, survivalCurve);

    return cdsPresentValueAtMidpoint;
  }

  // ------------------------------------------------------------------------

  /*
  // Private member function to convert the input ZonedDateTime tenors into doubles
  private double[] convertDatesToDoubles(ZonedDateTime valuationDate, ZonedDateTime[] tenors, DayCount dayCount) {

    int numberOfTenors = tenors.length;

    double[] tenorsAsDoubles = new double[numberOfTenors];

    for (int i = 0; i < numberOfTenors; i++) {

      tenorsAsDoubles[i] = TimeCalculator.getTimeBetween(valuationDate, tenors[i], dayCount);
    }

    return tenorsAsDoubles;
  }
  */

  // ------------------------------------------------------------------------
}
