/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to calibrate a single-name CDS survival curve to market observed term structure of par CDS spreads
 * The input is a vector of tenors and market observed par CDS spread quotes for those tenors
 * The output is a vector of tenors (represented as doubles) and the calibrated survival probabilities for those tenors
 */
public class CalibrateSurvivalCurve {

  // ------------------------------------------------------------------------

  // Set the tolerence for the root finder

  private static final double DEFAULT_TOLERENCE = 1e-10;
  private final double _tolerence;

  public CalibrateSurvivalCurve() {
    this(DEFAULT_TOLERENCE);
  }

  public CalibrateSurvivalCurve(double tolerence) {
    _tolerence = tolerence;
  }

  // ------------------------------------------------------------------------

  // TODO : Lots of work to do in here - Work In Progress
  // TODO : Add arg checkers to check that length of the tenor and parCDSSpreads vectors are the same
  // TODO : Check that the tenors are in ascending order
  // TODO : Check that the input par CDS spreads 'make sense' i.e. non-negative, NaN's etc
  // TODO : Add the interpolation and extrapolation methods
  // TODO : Is there a better way to set the tolerence?
  // TODO : Replace the root finder with something more sophisticated (bisection was used to ensure a root is found if it exists) 

  // ------------------------------------------------------------------------

  // Still a complete mess at the moment

  // Member function to calibrate a CDS objects survival curve to a term structure of market observed par CDS spreads
  // The input CDS object has all the schedule etc settings for computing the CDS's PV's etc
  // The user inputs the schedule of (future) dates on which we have observed par CDS spread quotes

  public double[] getCalibratedSurvivalCurve(CreditDefaultSwapDefinition cds, ZonedDateTime[] tenors, double[] marketSpreads, YieldCurve yieldCurve) {

    // Check input CDS and YieldCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(tenors, "Tenors field");

    // Check user data input is not null
    ArgumentChecker.notNull(marketSpreads, "par CDS spreads field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");

    int numberOfTenors = tenors.length;
    int numberOfSpreads = marketSpreads.length;

    // Get the valuation date of the CDS (should be equal to the adjusted effective date - check this)
    ZonedDateTime valuationDate = cds.getValuationDate();

    DayCount dayCount = cds.getDayCountFractionConvention();

    // Convert the ZonedDateTime tenors into doubles (measured from valuationDate)
    double[] tenorsAsDoubles = convertDatesToDoubles(valuationDate, tenors, dayCount);

    // These are the calibrated piecewise constant hazard rates that we compute from the solver
    double[] hazardRates = new double[numberOfTenors];

    double[] calibratedSurvivalCurve = new double[numberOfTenors + 1];

    //calibratedSurvivalCurve[0][0] = 0.0;
    //calibratedSurvivalCurve[0][1] = 1.0;

    // Create an object for getting the PV of a CDS
    final PresentValueCreditDefaultSwap presentValueCDS = new PresentValueCreditDefaultSwap();

    // Create a temporary CDS from the input CDS whose maturity and contractual spread will vary as we bootstrap the survival curve
    CreditDefaultSwapDefinition calibrationCDS = cds;

    // ----------------------------------------------------------------------------

    // Loop through each of the input tenors
    for (int m = 0; m < numberOfTenors; m++) {

      // Construct a temp vector of the first m tenors
      double[] runningTenors = new double[m + 1];

      // Populate this vector with the first m tenors (needed to construct the survival curve using these tenors)
      for (int i = 0; i <= m; i++) {
        runningTenors[i] = tenorsAsDoubles[i];
      }

      // Build a CDS with maturity of tenor[m] and spread marketSpread[m] 
      calibrationCDS = calibrationCDS.withMaturity(tenors[m]);
      calibrationCDS = calibrationCDS.withSpread(marketSpreads[m]);

      // Compute the calibrated hazard rate for this tenor
      double calibratedHazardRates = calibrateHazardRate(calibrationCDS, presentValueCDS, yieldCurve, runningTenors, hazardRates);

      hazardRates[m] = calibratedHazardRates;

      // Just return the hazard rates for now - replace with surv prob calcs
      calibratedSurvivalCurve[m] = hazardRates[m];
    }

    // ----------------------------------------------------------------------------

    return calibratedSurvivalCurve;
  }

  // ------------------------------------------------------------------------

  private double calibrateHazardRate(CreditDefaultSwapDefinition calibrationCDS,
      PresentValueCreditDefaultSwap presentValueCDS,
      YieldCurve yieldCurve,
      double[] runningTenors,
      double[] hazardRates) {

    int numberOfTenors = runningTenors.length;

    double alpha = 0.5;

    double calibratedHazardRate = 0.0;

    // Calculate the initial guess for the calibrated hazard rate (not used for simple bisection)
    double hazardRateGuess = (calibrationCDS.getParSpread() / 10000.0) / (1 - calibrationCDS.getCurveRecoveryRate());

    // Construct a survival curve using the first m tenors in runningTenors
    SurvivalCurve survivalCurve = new SurvivalCurve(runningTenors, hazardRates);

    SurvivalCurve tempCurve = new SurvivalCurve(runningTenors, hazardRates);

    // Extract out the hazard rate at tenor m which we wish to calibrate by varying its value
    calibratedHazardRate = survivalCurve.getHazardRates()[numberOfTenors - 1];

    // Now do the root search - simple bisection method

    double h1 = (1.0 - alpha) * hazardRateGuess;
    if (h1 < 0.0) {
      h1 = 0.0;
    }
    hazardRates[numberOfTenors - 1] = h1;
    tempCurve = tempCurve.bootstrapHelperSurvivalCurve(runningTenors, hazardRates);
    double f = presentValueCDS.getPresentValueCreditDefaultSwap(calibrationCDS, yieldCurve, tempCurve);

    double h2 = (1.0 + alpha) * hazardRateGuess;
    if (h2 > 1.0) {
      h2 = 1.0;
    }
    hazardRates[numberOfTenors - 1] = h2;
    tempCurve = tempCurve.bootstrapHelperSurvivalCurve(runningTenors, hazardRates);
    double fmid = presentValueCDS.getPresentValueCreditDefaultSwap(calibrationCDS, yieldCurve, tempCurve);

    // Check for f * fmid >= 0.0

    int jMax = 40;

    double dx = 0.0;

    double rtb = 0.0;

    if (f < 0.0) {
      dx = h2 - h1;
      rtb = h1;
    }
    else {
      dx = h1 - h2;
      rtb = h2;
    }

    for (int j = 1; j < jMax; j++) {

      dx = dx * 0.5;
      double hmid = rtb + dx;

      hazardRates[numberOfTenors - 1] = hmid;
      tempCurve = tempCurve.bootstrapHelperSurvivalCurve(runningTenors, hazardRates);
      fmid = presentValueCDS.getPresentValueCreditDefaultSwap(calibrationCDS, yieldCurve, tempCurve);

      if (fmid <= 0.0) {
        rtb = hmid;
      }

      if (Math.abs(dx) < _tolerence || hmid == 0.0) {
        return rtb;
      }
    }

    return 0.0;
  }

  // ------------------------------------------------------------------------

  // Private member function to convert the input ZonedDateTime tenors into doubles
  private double[] convertDatesToDoubles(ZonedDateTime valuationDate, ZonedDateTime[] tenors, DayCount dayCount) {

    int numberOfTenors = tenors.length;

    double[] tenorsAsDoubles = new double[numberOfTenors];

    for (int i = 0; i < numberOfTenors; i++) {

      tenorsAsDoubles[i] = TimeCalculator.getTimeBetween(valuationDate, tenors[i], dayCount);
    }

    return tenorsAsDoubles;
  }

  // ------------------------------------------------------------------------
}
