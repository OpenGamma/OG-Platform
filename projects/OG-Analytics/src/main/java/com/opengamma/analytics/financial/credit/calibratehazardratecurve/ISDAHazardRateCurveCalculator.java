/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.calibratehazardratecurve;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.CreditInstrumentDefinition;
import com.opengamma.analytics.financial.credit.CreditInstrumentDefinitionVisitorAdapter;
import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndSpreadsProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCollateralizedVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyFixedRecoveryCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyForwardStartingCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyRecoveryLockCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacySovereignCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCollateralizedVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardFixedRecoveryCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardForwardStartingCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardMuniCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardRecoveryLockCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardSovereignCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.standard.StandardVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda.ISDACreditDefaultSwapPVCalculator;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.financial.credit.util.CreditMarketDataUtils;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ISDAHazardRateCurveCalculator {
  private static final GenerateCreditDefaultSwapPremiumLegSchedule SCHEDULE_GENERATOR = new GenerateCreditDefaultSwapPremiumLegSchedule();
  private static final ISDACreditDefaultSwapPVCalculator PV_CALCULATOR = new ISDACreditDefaultSwapPVCalculator();
  private static final int DEFAULT_MAX_NUMBER_OF_ITERATIONS = 100;
  private static final double DEFAULT_TOLERANCE = 1e-10;
  private static final double DEFAULT_HAZARD_RATE_RANGE_MULTIPLIER = 0.5;
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final PriceType PRICE_TYPE = PriceType.CLEAN;
  private final int _maximumNumberOfIterations;
  private final double _tolerance;
  private final double _hazardRateRangeMultiplier;

  public ISDAHazardRateCurveCalculator() {
    this(DEFAULT_MAX_NUMBER_OF_ITERATIONS, DEFAULT_TOLERANCE, DEFAULT_HAZARD_RATE_RANGE_MULTIPLIER);
  }

  public ISDAHazardRateCurveCalculator(final int maximumNumberOfIterations, final double tolerance, final double hazardRateRangeMultiplier) {
    _tolerance = tolerance;
    _maximumNumberOfIterations = maximumNumberOfIterations;
    _hazardRateRangeMultiplier = hazardRateRangeMultiplier;
  }

  public HazardRateCurve calibrateHazardRateCurve(final CreditInstrumentDefinition cds, final ISDAYieldCurveAndSpreadsProvider data, final ZonedDateTime valuationDate) {
    ArgumentChecker.notNull(cds, "cds");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(valuationDate, "valuation date");
    final TempVisitor visitor = new TempVisitor(valuationDate);
    return cds.accept(visitor, data);
  }

  private class TempVisitor extends CreditInstrumentDefinitionVisitorAdapter<ISDAYieldCurveAndSpreadsProvider, HazardRateCurve> {
    private final ZonedDateTime _valuationDate;

    public TempVisitor(final ZonedDateTime valuationDate) {
      _valuationDate = valuationDate;
    }

    private HazardRateCurve calibrateCurve(final CreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      final ZonedDateTime[] marketDates = data.getMarketDates();
      final double[] marketSpreads = data.getMarketSpreads();
      final ISDADateCurve yieldCurve = data.getYieldCurve();
      final double[] calibratedHazardRates = getCalibratedHazardRateTermStructure(_valuationDate, cds, marketDates, marketSpreads, yieldCurve, PRICE_TYPE);
      final double[] modifiedHazardRateCurve = new double[calibratedHazardRates.length + 1];
      final double[] times = new double[marketDates.length + 1];
      modifiedHazardRateCurve[0] = calibratedHazardRates[0];
      times[0] = 0.0;
      for (int m = 1; m < modifiedHazardRateCurve.length; m++) {
        times[m] = ACT_365.getDayCountFraction(_valuationDate, marketDates[m - 1]);
        modifiedHazardRateCurve[m] = calibratedHazardRates[m - 1];
      }
      // Build a hazard rate curve object based on the input market data
      return new HazardRateCurve(marketDates, times, modifiedHazardRateCurve, 0.0);
    }

    private double[] getCalibratedHazardRateTermStructure(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ZonedDateTime[] marketDates,
        final double[] marketSpreads, final ISDADateCurve yieldCurve, final PriceType priceType) {
      // Check the efficacy of the input market data
      CreditMarketDataUtils.checkSpreadData(valuationDate, marketDates, marketSpreads);

      // Vector of (calibrated) piecewise constant hazard rates that we compute from the solver (this will have an element added to the end of it each time through the m loop below)
      final double[] hazardRates = new double[marketDates.length];

      // Convert the ZonedDateTime tenors into doubles (measured from valuationDate)
      final double[] tenorsAsDoubles = SCHEDULE_GENERATOR.convertTenorsToDoubles(marketDates, valuationDate, ACT_365);

      // Loop through each of the input tenors
      for (int m = 0; m < marketDates.length; m++) {
        // Construct a temporary vector of the first m tenors (note size of array)
        final int m1 = m + 1;
        final ZonedDateTime[] runningDates = new ZonedDateTime[m1];
        System.arraycopy(marketDates, 0, runningDates, 0, m1);
        final double[] runningTenorsAsDoubles = new double[m1];
        System.arraycopy(tenorsAsDoubles, 0, runningTenorsAsDoubles, 0, m1);
        // Construct a temporary vector of the hazard rates corresponding to the first m tenors (note size of array)
        final double[] runningHazardRates = new double[m1];
        System.arraycopy(hazardRates, 0, runningHazardRates, 0, m1);
        final CreditDefaultSwapDefinition calibrationCDS = getCDSWithSpread((cds.withMaturityDate(marketDates[m])), marketSpreads[m]);
        // Compute the calibrated hazard rate for tenor[m] (using the calibrated hazard rates for tenors 1, ..., m - 1)
        hazardRates[m] = calibrateHazardRate(calibrationCDS, yieldCurve, runningDates, runningTenorsAsDoubles, runningHazardRates, priceType, marketSpreads[m]);
      }
      return hazardRates;
    }

    //TODO remove this
    private CreditDefaultSwapDefinition getCDSWithSpread(final CreditDefaultSwapDefinition cds, final double spread) {
      if (cds instanceof LegacyCreditDefaultSwapDefinition) {
        return ((LegacyCreditDefaultSwapDefinition) cds).withSpread(spread);
      } else if (cds instanceof StandardCreditDefaultSwapDefinition) {
        return ((StandardCreditDefaultSwapDefinition) cds).withSpread(spread);
      }
      throw new IllegalArgumentException("Cannot handle CDS of type " + cds.getClass());
    }

    // Private method to do the root search to find the hazard rate for tenor m which gives the CDS a PV of zero
    private double calibrateHazardRate(final CreditDefaultSwapDefinition calibrationCDS, final ISDADateCurve yieldCurve, final ZonedDateTime[] marketTenors, final double[] runningTenors,
        final double[] hazardRates, final PriceType priceType, final double parSpread) {
      double deltaHazardRate = 0.0;
      double calibratedHazardRate = 0.0;
      // Calculate the initial guess for the calibrated hazard rate for this tenor
      final double hazardRateGuess = (parSpread / 10000.0) / (1 - calibrationCDS.getRecoveryRate());
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

      // Now do the root search (in hazard rate space) - simple bisection method for the moment (guaranteed to work and we are not concerned with speed at the moment)
      // Calculate the CDS PV at the lower hazard rate bound
      final double cdsPresentValueAtLowerPoint = calculateCDSPV(calibrationCDS, marketTenors, runningTenors, hazardRates, lowerHazardRate, yieldCurve, priceType);
      // Calculate the CDS PV at the upper hazard rate bound
      double cdsPresentValueAtMidPoint = calculateCDSPV(calibrationCDS, marketTenors, runningTenors, hazardRates, upperHazardRate, yieldCurve, priceType);

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
        cdsPresentValueAtMidPoint = calculateCDSPV(calibrationCDS, marketTenors, runningTenors, hazardRates, hazardRateMidpoint, yieldCurve, priceType);
        if (Double.doubleToLongBits(cdsPresentValueAtMidPoint) <= 0.0) {
          calibratedHazardRate = hazardRateMidpoint;
        }
        // Check to see if we have converged to within the specified tolerance or that we are at the root
        if (Math.abs(deltaHazardRate) < _tolerance || Double.doubleToLongBits(cdsPresentValueAtMidPoint) == 0.0) {
          return calibratedHazardRate;
        }
      }
      return 0.0;
    }

    // Private member function to compute the PV of a CDS given a particular guess for the hazard rate at tenor m (given calibrated hazard rates for tenors 0, ..., m - 1)
    private double calculateCDSPV(final CreditDefaultSwapDefinition calibrationCDS, final ZonedDateTime[] tenors,
        final double[] tenorsAsDoubles, final double[] hazardRates, final double hazardRateMidPoint, final ISDADateCurve yieldCurve, final PriceType priceType) {

      // How many tenors in the hazard rate term structure have been previously calibrated
      final int numberOfTenors = tenorsAsDoubles.length;

      // Put the hazard rate guess into the vector of hazard rates as the last element in the array
      hazardRates[numberOfTenors - 1] = hazardRateMidPoint;

      // Modify the survival curve so that it has the modified vector of hazard rates as an input to the ctor
      final HazardRateCurve hazardRateCurve = new HazardRateCurve(tenors, tenorsAsDoubles, hazardRates, 0);

      // Compute the PV of the CDS with this term structure of hazard rates
      final ISDAYieldCurveAndHazardRateCurveProvider curves = new ISDAYieldCurveAndHazardRateCurveProvider(yieldCurve, hazardRateCurve);
      return PV_CALCULATOR.getPresentValue(calibrationCDS, curves, _valuationDate, priceType);
    }

    @Override
    public HazardRateCurve visitStandardVanillaCDS(final StandardVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitStandardFixedRecoveryCDS(final StandardFixedRecoveryCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitStandardForwardStartingCDS(final StandardForwardStartingCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitStandardMuniCDS(final StandardMuniCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitStandardQuantoCDS(final StandardQuantoCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitStandardRecoveryLockCDS(final StandardRecoveryLockCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitStandardSovereignCDS(final StandardSovereignCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitStandardCollateralizedVanillaCDS(final StandardCollateralizedVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitLegacyVanillaCDS(final LegacyVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitLegacyFixedRecoveryCDS(final LegacyFixedRecoveryCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitLegacyForwardStartingCDS(final LegacyForwardStartingCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitLegacyMuniCDS(final LegacyMuniCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitLegacyQuantoCDS(final LegacyQuantoCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitLegacyRecoveryLockCDS(final LegacyRecoveryLockCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitLegacySovereignCDS(final LegacySovereignCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

    @Override
    public HazardRateCurve visitLegacyCollateralizedVanillaCDS(final LegacyCollateralizedVanillaCreditDefaultSwapDefinition cds, final ISDAYieldCurveAndSpreadsProvider data) {
      return calibrateCurve(cds, data);
    }

  }
}
