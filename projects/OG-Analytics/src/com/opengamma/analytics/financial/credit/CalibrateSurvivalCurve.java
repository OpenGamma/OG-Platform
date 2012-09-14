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

  private static final double DEFAULT_TOLERENCE = 1e-5;
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

  // ------------------------------------------------------------------------

  // Still a complete mess at the moment

  // Member function to calibrate a CDS objects survival curve to a term structure of market observed par CDS spreads
  // The input CDS object has all the schedule etc settings for computing the CDS's PV's etc
  // The user inputs the schedule of (future) dates on which we have observed par CDS spread quotes

  public double[][] getCalibratedSurvivalCurve(CreditDefaultSwapDefinition cds, ZonedDateTime[] tenors, double[] parCDSSpreads, YieldCurve yieldCurve) {

    // Check input CDS and YieldCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(tenors, "Tenors field");

    // Check user data input is not null
    ArgumentChecker.notNull(parCDSSpreads, "par CDS spreads field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");

    int numberOfTenors = tenors.length;
    int numberOfSpreads = parCDSSpreads.length;

    ZonedDateTime valuationDate = cds.getValuationDate();

    DayCount dayCount = cds.getDayCountFractionConvention();

    double[] test = convertDatesToDoubles(valuationDate, tenors, dayCount);

    /*
    for (int i = 0; i < test.length; i++) {
      System.out.println("i = " + i + "\t" + tenors[i] + "\t" + test[i]);
    }
    */

    // These are the calibrated piecewise constant hazard rates that we compute from the solver
    double[] hazardRates = new double[numberOfTenors];

    double[][] calibratedSurvivalCurve = new double[numberOfTenors + 1][2];

    double parSpread = 0.0;

    calibratedSurvivalCurve[0][0] = 0.0;
    calibratedSurvivalCurve[0][1] = 1.0;

    // Create an object for getting the par spread of a CDS
    final PresentValueCreditDefaultSwap bootstrapCDS = new PresentValueCreditDefaultSwap();

    // Create a temporary CDS whose maturity will vary as we bootstrap the survival curve
    CreditDefaultSwapDefinition currentCDS = cds;

    //System.out.println("Initial maturity date = " + currentCDS.getMaturityDate());

    // Create a survival curve
    SurvivalCurve survivalCurve = new SurvivalCurve(tenors, hazardRates);

    // Loop through each of the input tenors
    for (int m = 0; m < numberOfTenors; m++) {

      // Create vectors of size m to hold the first m tenors and haz rates
      ZonedDateTime[] tempTenors = new ZonedDateTime[m + 1];
      double[] tempHazRates = new double[m + 1];

      // 1st time through the loop, just have the 1st tenor

      // Copy the input tenors into the temp vector of size m (check limits of loop)
      for (int i = 0; i <= m; i++) {
        tempTenors[i] = tenors[i];
      }

      /*
      // 1st time through loop hazRates[0] = 0
      for (int i = 0; i <= m; i++) {
        tempHazRates[i] = hazardRates[i];
      }
      */

      // Initial hazard rate guess
      // hazRates[m] = guess[m];

      // Build a CDS with maturity of tenor[m]
      currentCDS = currentCDS.withMaturity(tenors[m]);

      // Build a survival curve using the calibrated haz rates up to tenor m (1 to m - 1 have been already calibrated) - m is initially a guess
      survivalCurve = survivalCurve.bootstrapHelperSurvivalCurve(tempTenors, hazardRates);

      // Now the root finding loop

      // while (parCDSSpreads[m] - parSpread > tolerance) {

      // guess h for tenor m (using the solver)

      // Calculate the par CDS spread for this guess for the haz rate term structure up to tenor[m]
      //parSpread = bootstrapCDS.getParSpreadCreditDefaultSwap(currentCDS, yieldCurve, survivalCurve);

      // end of the while loop

      // Record the hazard rate for tenor[m] of the term structure
      //hazardRates[m] = h;

    }

    return calibratedSurvivalCurve;
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
