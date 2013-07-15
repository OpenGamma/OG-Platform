/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to calibrate a single-name CDS hazard rate term structure to the market observed term structure of par CDS spreads
 * The input is a vector of tenors and market observed par CDS spread quotes for those tenors
 * The output is a vector of calibrated hazard rates for those tenors
 * 
 * This calibration method is applicable to legacy CDS
 */
public class CalibrateHazardRateCurveLegacyCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Create an object for calculating the premium leg schedule
  private static final GenerateCreditDefaultSwapPremiumLegSchedule SCHEDULE_GENERATOR = new GenerateCreditDefaultSwapPremiumLegSchedule();

  // Create an object for getting the PV of a CDS
  private static final PresentValueLegacyCreditDefaultSwap PV_CALCULATOR = new PresentValueLegacyCreditDefaultSwap();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Set the maximum number of iterations, tolerance and range of the hazard rate bounds for the root finder

  private static final int DEFAULT_MAX_NUMBER_OF_ITERATIONS = 100;
  private final int _maximumNumberOfIterations;

  private static final double DEFAULT_TOLERANCE = 1e-15;
  private final double _tolerance;

  private static final double DEFAULT_HAZARD_RATE_RANGE_MULTIPLIER = 0.5;
  private final double _hazardRateRangeMultiplier;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Ctor to initialise a CalibrateSurvivalCurve object with the default values for the root finder
  public CalibrateHazardRateCurveLegacyCreditDefaultSwap() {
    this(DEFAULT_MAX_NUMBER_OF_ITERATIONS, DEFAULT_TOLERANCE, DEFAULT_HAZARD_RATE_RANGE_MULTIPLIER);
  }

  // Ctor to initialise a CalibrateSurvivalCurve object with user specified values for the root finder
  public CalibrateHazardRateCurveLegacyCreditDefaultSwap(final int maximumNumberOfIterations, final double tolerance, final double hazardRateRangeMultiplier) {
    _tolerance = tolerance;
    _maximumNumberOfIterations = maximumNumberOfIterations;
    _hazardRateRangeMultiplier = hazardRateRangeMultiplier;
  }

  /*
  public HazardRateCurve getCalibratedHazardRateCurve(final ZonedDateTime calibrationDate, final LegacyVanillaCreditDefaultSwapDefinition cds, final ZonedDateTime[] tenors,
      final double[] marketSpreads,
      final ISDACurve yieldCurve, final PriceType priceType) {
    ArgumentChecker.notNull(calibrationDate, "calibration date");
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(tenors, "tenors");
    ArgumentChecker.notNull(marketSpreads, "market spreads");
    ArgumentChecker.notNull(yieldCurve, "yield curve");
    ArgumentChecker.isTrue(tenors.length == marketSpreads.length, "number of tenors {} and market spreads {} should be equal", tenors.length, marketSpreads.length);
    final double[] hazardRates = new double[tenors.length];
    final double[] tenorsAsDoubles = SCHEDULE_GENERATOR.convertTenorsToDoubles(tenors, calibrationDate, DayCountFactory.INSTANCE.getDayCount("ACT/365"));
    LegacyVanillaCreditDefaultSwapDefinition calibrationCDS = cds;
    for (int m = 0; m < tenors.length; m++) {
      ArgumentChecker.isTrue(tenors[m].isAfter(calibrationDate), "Calibration instrument of tenor {} is before the valuation date {}", tenors[m], calibrationDate);
      if (tenors.length > 1 && m > 0) {
        ArgumentChecker.isTrue(tenors[m].isAfter(tenors[m - 1]), "Tenors not in ascending order");
      }
      ArgumentChecker.notNegative(marketSpreads[m], "Market spread at tenor " + tenors[m]);
      ArgumentChecker.notZero(marketSpreads[m], _tolerance, "Market spread at tenor " + tenors[m]);
      final double[] runningTenors = new double[m + 1];
      final double[] runningHazardRates = new double[m + 1];
      for (int i = 0; i <= m; i++) {
        runningTenors[i] = tenorsAsDoubles[i];
        runningHazardRates[i] = hazardRates[i];
      }
      //calibrationCDS = calibrationCDS.withMaturityDate(tenors[m]);
      //calibrationCDS = calibrationCDS.withSpread(marketSpreads[m]);
      hazardRates[m] = calibrateHazardRate(calibrationDate, calibrationCDS, yieldCurve, runningTenors, runningHazardRates, priceType);
    }
    return new HazardRateCurve(tenorsAsDoubles, hazardRates, 0);
  }
  */

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Replace the root finder with something more sophisticated (bisection was used to ensure a root is found if it exists - speed is not a concern at the moment)
  // TODO : Add a method to convert the hazard rates to survival probabilities
  // TODO : Currently only implementing piecewise constant hazard rate term structure assumption (market standard approach). Need to add further choices in due course.
  // TODO : Not happy with the structure of this solution (would prefer to input and return a DoublesCurve object not a single vector) - need to revisit
  // TODO : There is a problem with the accrued payment when calibrating a term structure - need to fix

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member function to calibrate a CDS objects hazard rate term structure to a term structure of market observed par CDS spreads
  // The input CDS object has all the schedule etc settings for computing the CDS's PV's etc
  // The user inputs the schedule of (future) dates on which we have observed par CDS spread quotes

  public double[] getCalibratedHazardRateTermStructure(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds, // Pass in a Legacy CDS object
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check the input arguments

    // Check input CDS and YieldCurve objects are not null
    ArgumentChecker.notNull(valuationDate, "valuation date");
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");

    // Check user data input is not null
    ArgumentChecker.notNull(marketTenors, "Tenors field");
    ArgumentChecker.notNull(marketSpreads, "Market observed CDS spreads field");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector of (calibrated) piecewise constant hazard rates that we compute from the solver (this will have an element added to the end of it each time through the m loop below)
    final double[] hazardRates = new double[marketTenors.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Convert the ZonedDateTime tenors into doubles (measured from valuationDate)
    final double[] tenorsAsDoubles = SCHEDULE_GENERATOR.convertTenorsToDoubles(marketTenors, valuationDate, DayCountFactory.INSTANCE.getDayCount("ACT/365"));

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a calibration CDS object from the input CDS (maturity and contractual spread of this CDS will vary as we bootstrap up the hazard rate term structure)
    LegacyCreditDefaultSwapDefinition calibrationCDS = cds;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop through each of the input tenors
    for (int m = 0; m < marketTenors.length; m++) {

      // Construct a temporary vector of the first m tenors (note size of array)
      final ZonedDateTime[] runningTenors = new ZonedDateTime[m + 1];
      final double[] runningTenorsAsDoubles = new double[m + 1];

      // Construct a temporary vector of the hazard rates corresponding to the first m tenors (note size of array)
      final double[] runningHazardRates = new double[m + 1];

      // Populate these vector with the first m tenors (needed to construct the survival curve using these tenors)
      for (int i = 0; i <= m; i++) {
        runningTenors[i] = marketTenors[i];
        runningTenorsAsDoubles[i] = tenorsAsDoubles[i];
        runningHazardRates[i] = hazardRates[i];
      }

      // Modify the calibration CDS to have a maturity of tenor[m]
      calibrationCDS = (LegacyCreditDefaultSwapDefinition) calibrationCDS.withMaturityDate(marketTenors[m]);

      // Modify the calibration CDS to have a contractual spread of marketSpread[m]
      calibrationCDS = calibrationCDS.withSpread(marketSpreads[m]);

      // Compute the calibrated hazard rate for tenor[m] (using the calibrated hazard rates for tenors 1, ..., m - 1)
      hazardRates[m] = calibrateHazardRate(valuationDate, calibrationCDS, yieldCurve, runningTenors, runningTenorsAsDoubles, runningHazardRates, priceType);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return hazardRates;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Private method to do the root search to find the hazard rate for tenor m which gives the CDS a PV of zero

  private double calibrateHazardRate(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition calibrationCDS,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] runningTenors,
      final double[] hazardRates,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double deltaHazardRate = 0.0;
    double calibratedHazardRate = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the initial guess for the calibrated hazard rate for this tenor
    double hazardRateGuess = (calibrationCDS.getParSpread() / 10000.0) / (1 - calibrationCDS.getRecoveryRate());

    if (hazardRateGuess > 1.0) {
      hazardRateGuess = 0.90;
    }

    // Calculate the initial bounds for the hazard rate search
    double lowerHazardRate = (1.0 - _hazardRateRangeMultiplier) * hazardRateGuess;
    double upperHazardRate = (1.0 + _hazardRateRangeMultiplier) * hazardRateGuess;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Make sure the initial hazard rate bounds are in the range [0, 1] (otherwise would have arbitrage)
    if (lowerHazardRate < 0.0) {
      lowerHazardRate = 0.0;
    }

    if (upperHazardRate > 1.0) {
      upperHazardRate = 1.0;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct a hazard rate term structure curve using the (calibrated) first m tenors in runningTenors
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(marketTenors, runningTenors, hazardRates, 0.0);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // TODO : For testing purposes only - remember to take out
    final double temp = calculateCDSPV(valuationDate, calibrationCDS, marketTenors, runningTenors, hazardRates, hazardRateGuess, yieldCurve, hazardRateCurve, priceType);

    // Now do the root search (in hazard rate space) - simple bisection method for the moment (guaranteed to work and we are not concerned with speed at the moment)

    // Calculate the CDS PV at the lower hazard rate bound
    final double cdsPresentValueAtLowerPoint = calculateCDSPV(valuationDate, calibrationCDS, marketTenors, runningTenors, hazardRates, lowerHazardRate, yieldCurve, hazardRateCurve, priceType);

    // Calculate the CDS PV at the upper hazard rate bound
    double cdsPresentValueAtMidPoint = calculateCDSPV(valuationDate, calibrationCDS, marketTenors, runningTenors, hazardRates, upperHazardRate, yieldCurve, hazardRateCurve, priceType);

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
      final double hazardRateMidpoint = calibratedHazardRate + deltaHazardRate;

      // Calculate the CDS PV at the hazard rate range midpoint
      cdsPresentValueAtMidPoint = calculateCDSPV(valuationDate, calibrationCDS, marketTenors, runningTenors, hazardRates, hazardRateMidpoint, yieldCurve, hazardRateCurve, priceType);

      if (Double.doubleToLongBits(cdsPresentValueAtMidPoint) <= 0.0) {
        calibratedHazardRate = hazardRateMidpoint;
      }

      // Check to see if we have converged to within the specified tolerance or that we are at the root
      if (Math.abs(deltaHazardRate) < _tolerance || Double.doubleToLongBits(cdsPresentValueAtMidPoint) == 0.0) {
        return calibratedHazardRate;
      }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return 0.0;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Private member function to compute the PV of a CDS given a particular guess for the hazard rate at tenor m (given calibrated hazard rates for tenors 0, ..., m - 1)

  private double calculateCDSPV(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition calibrationCDS,
      final ZonedDateTime[] tenors,
      final double[] tenorsAsDoubles,
      final double[] hazardRates,
      final double hazardRateMidPoint,
      final ISDADateCurve yieldCurve,
      HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // How many tenors in the hazard rate term structure have been previously calibrated
    final int numberOfTenors = tenorsAsDoubles.length;

    // Put the hazard rate guess into the vector of hazard rates as the last element in the array
    hazardRates[numberOfTenors - 1] = hazardRateMidPoint;

    // Modify the survival curve so that it has the modified vector of hazard rates as an input to the ctor
    hazardRateCurve = hazardRateCurve.bootstrapHelperHazardRateCurve(tenors, tenorsAsDoubles, hazardRates);

    // Compute the PV of the CDS with this term structure of hazard rates
    final double cdsPresentValueAtMidpoint = PV_CALCULATOR.getPresentValueLegacyCreditDefaultSwap(valuationDate, calibrationCDS, yieldCurve, hazardRateCurve, priceType);

    return cdsPresentValueAtMidpoint;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
