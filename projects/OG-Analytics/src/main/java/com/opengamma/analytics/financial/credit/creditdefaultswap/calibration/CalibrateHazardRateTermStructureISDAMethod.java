/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.calibration;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class for calculating a term structure of hazard rates calibrated to a 
 * set of SNCDS calibration instruments using the ISDA methodology
 */
public class CalibrateHazardRateTermStructureISDAMethod {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private static final PresentValueCreditDefaultSwap cdsCalculator = new PresentValueCreditDefaultSwap();

  // Constants associated with the ISDA root finder
  private static final int numIterations = 100;

  private static final double boundLo = 0.0;
  private static final double boundHi = 1e10;

  private static double initialXstep = 0.0005;
  private static final double initialFDeriv = 0;

  private static final double xacc = 1e-10;
  private static final double facc = 1e-10;

  private static final double ONE_PER_CENT = 0.01;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to change the type of CDS object passed into cdsBootstrapPointFunction (only passing in a legacy CDS so we can access its par spread)
  // TODO : Need to change the type of CDS object passed into isdaRootFinder
  // TODO : Need to change the type of CDS object passed into brentMethod and secant
  // TODO : Check the input arguments (not null, market data compatible etc) in all the functions
  // TODO : The  returning of results in secant is a bit of a hack because we are passing back TRUE/FALSE information through the return doubles vector
  // TODO : Need to revisit the exact calculation of the hazard rate term structure dates (make sure absolutely clear on this)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The ISDA calibration routine (this is the equivalent of the 'CdsBootstrap' function in the ISDA code)

  public HazardRateCurve isdaCalibrateHazardRateCurve(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve) {

    ArgumentChecker.notNull(valuationDate, "valuation date");
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(marketTenors, "marketTenors");
    ArgumentChecker.notNull(marketSpreads, "marketSpreads");
    ArgumentChecker.notNull(yieldCurve, "yieldCurve");
    
    
    // ----------------------------------------------------------------------------------------------------------------------------------------

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

    // ----------------------------------------------------------------------------------------------------------------------------------------

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
      calibratedHazardRateCurve[i] = isdaRootFinder(valuationDate, calibrationCDS, yieldCurve, runningHazardRateCurve, guess, PriceType.CLEAN);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct the curve from the calibrated hazard rates

    final double[] modifiedHazardRateCurve = new double[calibratedHazardRateCurve.length + 1];

    modifiedHazardRateCurve[0] = calibratedHazardRateCurve[0];

    for (int m = 1; m < modifiedHazardRateCurve.length; m++) {
      modifiedHazardRateCurve[m] = calibratedHazardRateCurve[m - 1];
    }

    // Now build the complete, calibrated hazard rate curve
    HazardRateCurve hazardRateCurve = new HazardRateCurve(marketTenors, tenorsAsDoubles, modifiedHazardRateCurve, 0.0);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return hazardRateCurve;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // This code is simply adapted from the ISDA code. No attempt has been made to make it more logical or correct e.g. using Double.tobits

  private double isdaRootFinder(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final double guess,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double[] xPoints = new double[3];
    double[] yPoints = new double[3];

    xPoints[0] = guess;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    if (boundLo >= boundHi) {
      throw new OpenGammaRuntimeException("Could not calibrate hazard rate curve");
    }

    if (xPoints[0] < boundLo || xPoints[0] > boundHi) {
      throw new OpenGammaRuntimeException("Could not calibrate hazard rate curve");
    }

    // Calc the value of the objective function at the initial hazard rate guess i.e. using the hazard rate curve as input
    yPoints[0] = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, hazardRateCurve, priceType);

    if (yPoints[0] == 0.0 || (Math.abs(yPoints[0]) <= facc && (Math.abs(boundLo - xPoints[0]) <= xacc || Math.abs(boundHi - xPoints[0]) <= xacc))) {
      return xPoints[0];
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double boundSpread = boundHi - boundLo;

    if (initialXstep == 0.0) {
      initialXstep = ONE_PER_CENT * boundSpread;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

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

    // ----------------------------------------------------------------------------------------------------------------------------------------

    HazardRateCurve modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, xPoints[2]);

    yPoints[2] = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, priceType);

    if (yPoints[2] == 0.0 || (Math.abs(yPoints[2]) <= facc && Math.abs(xPoints[2] - xPoints[0]) <= xacc)) {
      return xPoints[2];
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // This is terrible code, but it has to be absolutely comparable with the ISDA model calcs otherwise  
    // we will get small differences in the calibrated hazard rates

    final double[] secantSearch = secant(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, numIterations, xacc, facc, boundLo, boundHi, xPoints, yPoints);

    if (secantSearch[1] == 1.0) {
      // Found the root
      return secantSearch[2];
    } else if (secantSearch[0] == 1.0) {
      // Didn't find the root, but it was bracketed

      // Do we pass in the modifiedHazardRateCurve ?
      final double root = brentMethod(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, numIterations, xacc, facc, xPoints, yPoints);

      return root;
    } else {
      // Root was not found or bracketed, now try at the bounds

      modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, boundLo);

      final double fLo = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, priceType);

      if (fLo == 0.0 || (Math.abs(fLo) <= facc && Math.abs(boundLo - xPoints[0]) <= xacc)) {
        return boundLo;
      }

      if (yPoints[0] * fLo < 0) {
        xPoints[2] = xPoints[0];
        xPoints[0] = boundLo;

        yPoints[2] = yPoints[0];
        yPoints[0] = fLo;
      } else {
        modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, boundHi);

        final double fHi = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, priceType);

        if (fHi == 0.0 || (Math.abs(fHi) <= facc && Math.abs(boundHi - xPoints[0]) <= xacc))
        {
          return boundHi;
        }

        if (yPoints[0] * fHi < 0) {
          xPoints[2] = boundHi;
          yPoints[2] = fHi;
        } else {
          // If the algorithm gets here the root has not been found, need to make sure it reports its failure and falls over
          throw new OpenGammaRuntimeException("Could not calibrate hazard rate curve");
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

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // If the algorithm gets here the root has not been found, need to make sure it reports its failure and falls over
    throw new OpenGammaRuntimeException("Could not calibrate hazard rate curve");
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

      if (f2 * f1 > 0.0) {
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

      if (f3 * f31 < ratio * f2 * f21 || f21 == 0. || f31 == 0. || f32 == 0.) {
        x3 = x2;
        f3 = f2;
      } else {
        xm = x1 - (f1 / f21) * x21 + ((f1 * f2) / (f31 * f32)) * x31 - ((f1 * f2) / (f21 * f32)) * x21;

        HazardRateCurve modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, xm);

        // NOTE : Passing in the PriceType variable to this calculation
        fm = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, PriceType.CLEAN);

        if (fm == 0.0 || (Math.abs(fm) <= facc && Math.abs(xm - x1) <= xacc)) {
          return xm;
        }

        if (fm * f1 < 0.0) {
          x3 = xm;
          f3 = fm;
        } else {
          x1 = xm;
          f1 = fm;
          x3 = x2;
          f3 = f2;
        } // End if fm*f1<0.0 else ...

      } // End if f3*f31 < ratio*f2*f21 || f21 == 0. || f31 == 0. || f32 == 0. else ...

      x2 = 0.5 * (x1 + x3);

      HazardRateCurve modifiedHazardRateCurve = modifyHazardRateCurve(hazardRateCurve, x2);

      f2 = cdsBootstrapPointFunction(valuationDate, cds, yieldCurve, modifiedHazardRateCurve, PriceType.CLEAN);

      if (f2 == 0.0 || (Math.abs(f2) <= facc && Math.abs(x2 - x1) <= xacc)) {
        return x2;
      }

    } // End loop over j

    // If the algorithm gets here the maximum number of iterations has been exceeded, need to make sure it reports its failure and falls over
    throw new OpenGammaRuntimeException("Could not calibrate hazard rate curve");
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

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

      if (Math.abs(yPoints[0] - yPoints[2]) <= facc) {
        if (yPoints[0] - yPoints[2] > 0) {
          dx = -yPoints[0] * (xPoints[0] - xPoints[2]) / facc;
        } else {
          dx = yPoints[0] * (xPoints[0] - xPoints[2]) / facc;
        }
      } else {
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
          (yPoints[0] > 0 && yPoints[1] > 0 && yPoints[2] > 0)) {
        if (Math.abs(yPoints[0]) > Math.abs(yPoints[1])) {
          xPoints[2] = xPoints[0];
          yPoints[2] = yPoints[0];
          xPoints[0] = xPoints[1];
          yPoints[0] = yPoints[1];
        } else {
          xPoints[2] = xPoints[1];
          yPoints[2] = yPoints[1];
        }
        continue;
      } else {
        if (yPoints[0] * yPoints[2] > 0) {
          if (xPoints[1] < xPoints[0]) {
            double tempX = xPoints[0];
            double tempY = yPoints[0];

            xPoints[0] = xPoints[1];
            xPoints[1] = tempX;

            yPoints[0] = yPoints[1];
            yPoints[1] = tempY;
          } else {
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

  private HazardRateCurve modifyHazardRateCurve(final HazardRateCurve hazardRateCurve, final double h) {

    // Extract out the components of the hazard rate curve
    final ZonedDateTime[] hazardCurveTenors = hazardRateCurve.getCurveTenors();
    final double[] hazardCurveTimes = hazardRateCurve.getTimes();
    final double[] hazardCurveRates = hazardRateCurve.getRates();

    // Replace the final point on the piecewise calibrated hazard rate curve with the updated value
    hazardCurveRates[hazardCurveRates.length - 1] = h;

    // Return an updated hazard rate curve
    return hazardRateCurve.bootstrapHelperHazardRateCurve(hazardCurveTenors, hazardCurveTimes, hazardCurveRates);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double cdsBootstrapPointFunction(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // NOTE : We divide the values returned from the premium and contingent leg calculations by the trade notional. This is only for the purposes of the calibration
    // NOTE : routine because we require a unit notional (so that the comparison with the accuracy variables are meaningful)

    // Compute the PV of the premium leg
    final double presentValuePremiumLeg = (cds.getParSpread() / 10000) * cdsCalculator.calculatePremiumLeg(valuationDate, cds, yieldCurve, hazardRateCurve, PriceType.CLEAN) / cds.getNotional();

    // Compute the PV of the contingent leg
    final double presentValueContingentLeg = cdsCalculator.calculateContingentLeg(valuationDate, cds, yieldCurve, hazardRateCurve) / cds.getNotional();

    // Compute the CDS PV
    final double presentValue = presentValueContingentLeg - presentValuePremiumLeg;

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
