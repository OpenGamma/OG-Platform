/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpers;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.calibration.CalibrateIndexCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.pricing.PresentValueIndexCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.underlyingpool.definition.UnderlyingPool;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of RecoveryRate01 for an index CDS
 */
public class RecRate01IndexCreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to sort out the breakevenSpreads input to the pricer
  // TODO : Need to sort out the use of LegacyVanillaCreditDefaultSwapDefinition object and cast

  // ------------------------------------------------------------------------------------------------------------------------------------------

  private static final CalibrateIndexCreditDefaultSwap calibrateHazardRateCurves = new CalibrateIndexCreditDefaultSwap();

  // Construct an index present value calculator object
  private static final PresentValueIndexCreditDefaultSwap indexPresentValue = new PresentValueIndexCreditDefaultSwap();

  private static final RecoveryRateBumpers recoveryRateBumper = new RecoveryRateBumpers();

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the RecRate by a bump to the recovery rates of each of the obligors in the index - bump is applied simultaneously to all obligors

  public double getRecRate01IndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurves,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double recoveryRateBump,
      final RecoveryRateBumpType recoveryRateBumpType) {

    // ------------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurves, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(recoveryRateBumpType, "Recovery rate bump type");

    // ------------------------------------------------------------------------------------------------------------------------------------------

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double[] breakevenSpreads = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      breakevenSpreads[i] = marketSpreads[i][0];
    }

    // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index to the unbumped credit spreads
    final HazardRateCurve[] hazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, indexCDS, marketTenors, marketSpreads, yieldCurves);

    // Calculate the unbumped value of the index
    final double presentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, indexCDS, breakevenSpreads, yieldCurves, hazardRateCurves);

    // Extract out the underlying pool from the index CDS
    UnderlyingPool modifiedPool = indexCDS.getUnderlyingPool();

    double[] recoveryRates = modifiedPool.getRecoveryRates();

    //final double recoveryRate = modifiedPool.getRecoveryRates()[i];

    final double[] bumpedRecoveryRates = recoveryRateBumper.getBumpedRecoveryRate(recoveryRates, recoveryRateBump, recoveryRateBumpType);

    // Modify the temp pool object to have the bumped recovery rate vector
    modifiedPool = modifiedPool.withRecoveryRates(bumpedRecoveryRates);

    // Construct a temporary index CDS
    IndexCreditDefaultSwapDefinition tempIndex = indexCDS;

    // Modify the temporary index to have the modified underlying pool
    tempIndex = tempIndex.withUnderlyingPool(modifiedPool);

    // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index to the bumped recovery rates
    final HazardRateCurve[] bumpedHazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, tempIndex, marketTenors, marketSpreads, yieldCurves);

    // Calculate the bumped value of the index
    final double bumpedPresentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, tempIndex, breakevenSpreads, yieldCurves, bumpedHazardRateCurves);

    final double recRate01 = (bumpedPresentValue - presentValue) / recoveryRateBump;

    return recRate01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the RecRate by a bump to the recovery rates of each of the obligors in the index - bump is applied individually to all obligors one by one

  public double[] getRecRate01ByObligorIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurves,
      final ZonedDateTime[] marketTenors,
      final double[][] marketSpreads,
      final double recoveryRateBump,
      final RecoveryRateBumpType recoveryRateBumpType) {

    // ------------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurves, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(recoveryRateBumpType, "Recovery rate bump type");

    // ------------------------------------------------------------------------------------------------------------------------------------------

    int numberOfObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double[] recRate01 = new double[numberOfObligors];

    double[] breakevenSpreads = new double[numberOfObligors];

    for (int i = 0; i < numberOfObligors; i++) {
      breakevenSpreads[i] = marketSpreads[i][0];
    }

    // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index to the unbumped credit spreads
    final HazardRateCurve[] hazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, indexCDS, marketTenors, marketSpreads, yieldCurves);

    // Calculate the unbumped value of the index
    final double presentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, indexCDS, breakevenSpreads, yieldCurves, hazardRateCurves);

    for (int i = 0; i < numberOfObligors; i++) {

      // TODO : Suspect there is a bug here in how the recovery rate is being bumped

      // Extract out the underlying pool from the index CDS
      UnderlyingPool modifiedPool = indexCDS.getUnderlyingPool();

      double[] bumpedRecoveryRates = modifiedPool.getRecoveryRates();

      final double recoveryRate = modifiedPool.getRecoveryRates()[i];

      bumpedRecoveryRates[i] = recoveryRateBumper.getBumpedRecoveryRate(recoveryRate, recoveryRateBump, recoveryRateBumpType);

      // Modify the temp pool object to have the bumped recovery rate vector
      modifiedPool = modifiedPool.withRecoveryRates(bumpedRecoveryRates);

      // Construct a temporary index CDS
      IndexCreditDefaultSwapDefinition tempIndex = indexCDS;

      // Modify the temporary index to have the modified underlying pool
      tempIndex = tempIndex.withUnderlyingPool(modifiedPool);

      // Calibrate the hazard rate term structures of each of the obligors in the pool underlying the index to the bumped recovery rates
      final HazardRateCurve[] bumpedHazardRateCurves = calibrateHazardRateCurves.getCalibratedHazardRateCurves(valuationDate, tempIndex, marketTenors, marketSpreads, yieldCurves);

      // Calculate the bumped value of the index
      final double bumpedPresentValue = indexPresentValue.getIntrinsicPresentValueIndexCreditDefaultSwap(valuationDate, tempIndex, breakevenSpreads, yieldCurves, bumpedHazardRateCurves);

      // Calculate the recovery rate sensitivity of the index CDS to a bump in obligor i's recovery rate
      recRate01[i] = (bumpedPresentValue - presentValue);
    }

    return recRate01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
