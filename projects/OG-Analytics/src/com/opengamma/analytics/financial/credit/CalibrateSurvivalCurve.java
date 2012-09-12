/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to calibrate a single-name CDS survival curve to market observed term structure of par CDS spreads
 * The input is a vector of tenors and market observed par CDS spread quotes for those tenors
 * The output is a vector of tenors (represented as doubles) and the calibrated survival probabilities for those tenors
 */
public class CalibrateSurvivalCurve {

  // ------------------------------------------------------------------------

  // TODO : Lots of work to do in here - Work In Progress
  // TODO : Add arg checkers to check that length of the tenor and parCDSSpreads vectors are the same
  // TODO : Check that the tenors are in ascending order
  // TODO : Check that the input par CDS spreads 'make sense' i.e. non-negative, NaN's etc
  // TODO : Add the interpolation and extrapolation methods

  // ------------------------------------------------------------------------

  // Member function to calibrate a CDS objects survival curve to a term structure of market observed par CDS spreads
  public double[][] getCalibratedSurvivalCurve(CreditDefaultSwapDefinition cds, ZonedDateTime[] tenors, double[] parCDSSpreads, YieldCurve yieldCurve) {

    // Check input CDS and YieldCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(tenors, "Tenors field");
    ArgumentChecker.notNull(parCDSSpreads, "par CDS spreads field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");

    int numberOfTenors = tenors.length;
    int numberOfSpreads = parCDSSpreads.length;

    double[] hazardRates = new double[numberOfTenors];
    double[][] survivalCurve = new double[numberOfTenors + 1][2];

    double valuationRecoveryRate = cds.getCurveRecoveryRate();

    survivalCurve[0][0] = 1.0;

    // Create a temporary CDS whose maturity will vary as we bootstrap the survival curve
    CreditDefaultSwapDefinition currentCDS = cds;

    //System.out.println("Initial maturity date = " + currentCDS.getMaturityDate());

    /*
    for (int m = 0; m < numberOfTenors; m++) {

      ZonedDateTime currentTenor = tenors[m];

      //System.out.println(currentTenor);

      currentCDS.withMaturity(null);

      ZonedDateTime temp = currentCDS.getMaturityDate();

      System.out.println("Current maturity date = " + temp);

    }
    */

    /*
    // Create a temporary CDS object identical to the input object
    CreditDefaultSwapDefinition tempCDS = cds;

    tempCDS.withMaturity(null);

    // Main loop of calibration routine
    for (int i = 0; i < numberOfTenors; i++) {

      // Need to build a CDS with a tenor of tenor[i]

      ZonedDateTime maturityDate = cds.getMaturityDate();
      ZonedDateTime tempMaturityDate = tempCDS.getMaturityDate();

      //final PresentValueCreditDefaultSwap runningCDS = new PresentValueCreditDefaultSwap();
      //double pV = runningCDS.getPresentValueCreditDefaultSwap(cds);

    }
    */

    return survivalCurve;
  }

  // ------------------------------------------------------------------------

  // Member function to get the survival probability at time t from a calibrated survival curve
  public double getSurvivalProbability(double t) {

    double survivalProbability = 0.0;

    return survivalProbability;
  }

  // ------------------------------------------------------------------------

  // Member function to get the hazard rate at time t from a calibrated survival curve
  public double getHazardRate(double t) {

    double hazardRate = 0.0;

    return hazardRate;
  }

  // ------------------------------------------------------------------------

  private double getSurvivalProbability(double[] tenors, double[] hazardRates, double t) {

    double runningTotal = 0.0;
    double survivalProbability = 0.0;

    int counter = 0;

    while (t <= tenors[counter]) {

      runningTotal += hazardRates[counter] * tenors[counter];
      counter++;
    }

    survivalProbability = Math.exp(-runningTotal);

    return survivalProbability;

  }

  // Private member function to convert the input tenors into doubles
  private double[] convertDatesToDoubles(ZonedDateTime[] tenors) {

    int numberOfTenors = tenors.length;

    double[] tenorsAsDoubles = new double[numberOfTenors];

    return tenorsAsDoubles;
  }

  // ------------------------------------------------------------------------
}
