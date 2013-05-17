/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.calibration;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.calibration.CalibrateHazardRateTermStructureISDAMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;

/**
 * Class to undertake the calibration of the single name CDS's that constitute a CDS index
 */
public class CalibrateIndexCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Check that the number of columns in marketSpreads is equal to the length of calibrationTenors
  // TODO : Add the arg checkers for the inputs

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Create a CDS PV calculator object (this is used in the calibration of the survival probabilities)
  private static final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

  // Create an object for calibrating a SNCDS
  private static final CalibrateHazardRateTermStructureISDAMethod cdsCalibrator = new CalibrateHazardRateTermStructureISDAMethod();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Function to calibrate a pool of obligor hazard rate term structures to the input market data

  public HazardRateCurve[] getCalibratedHazardRateCurves(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ZonedDateTime[] calibrationTenors,
      final double[][] marketSpreads,
      final ISDADateCurve[] yieldCurves) {

    // Determine how many obligors there are in the underlying pool
    final int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    // Construct a vector of hazard rate curves of this size
    HazardRateCurve[] hazardRateCurves = new HazardRateCurve[numberOfObligors];

    // Calibrate each of the underlying obligors
    for (int i = 0; i < numberOfObligors; i++) {

      // Create a copy of the underlying CDS for obligor i for the purposes of calibration of the hazard rate term structure
      final LegacyVanillaCreditDefaultSwapDefinition underlyingCalibrationCDS = (LegacyVanillaCreditDefaultSwapDefinition) indexCDS.getUnderlyingCDS()[i];

      final double[] obligorMarketSpreads = new double[calibrationTenors.length];

      // From the input matrix of spread data, extract out the spread term structure for obligor i
      for (int m = 0; m < calibrationTenors.length; m++) {
        obligorMarketSpreads[m] = marketSpreads[i][m];
      }

      // Build a hazard rate curve object based on the input market data
      //hazardRateCurves[i] = creditDefaultSwap.calibrateHazardRateCurve(valuationDate, underlyingCalibrationCDS, calibrationTenors, obligorMarketSpreads, yieldCurves[i]);
      hazardRateCurves[i] = cdsCalibrator.isdaCalibrateHazardRateCurve(valuationDate, underlyingCalibrationCDS, calibrationTenors, obligorMarketSpreads, yieldCurves[i]);
    }

    return hazardRateCurves;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
