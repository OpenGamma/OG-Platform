/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.pricing;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the valuation of an index CDS
 */
public class PresentValueIndexCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work - in - Progress

  // TODO : Where do we carry out the calibration of the CDS's in the underlying pool?
  // TODO : Make sure the Buy/sell convention is coded correctly

  // NOTE : We pass in market data as vectors of objects. That is, we pass in a vector of yieldCurves
  // NOTE : (hence in principle each Obligor in the UnderlyingPool can have their cashflows discounted
  // NOTE : by a different discount curve) and we pass in a vector of calibrated hazard rate term structures
  // NOTE : (one for each Obligor in the UnderlyingPool)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the present value of an Index CDS position

  public double getPresentValueIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final double indexSpread,
      final ISDADateCurve[] yieldCurves,
      final HazardRateCurve[] hazardRateCurves) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurves, "Yield Curves");
    ArgumentChecker.notNull(hazardRateCurves, "Hazard Rate Curves");

    ArgumentChecker.notNegative(indexSpread, "Index spread");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the value of the premium leg (including accrued if required)
    double presentValueIndexPremiumLeg = calculateIndexPremiumLeg(valuationDate, indexCDS, yieldCurves, hazardRateCurves);

    // Calculate the value of the contingent leg
    double presentValueIndexContingentLeg = calculateIndexContingentLeg(valuationDate, indexCDS, yieldCurves, hazardRateCurves);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -presentValueIndexPremiumLeg + presentValueIndexContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (indexCDS.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the present value of an Index CDS premium leg
  private double calculateIndexPremiumLeg(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurves,
      final HazardRateCurve[] hazardRateCurves) {

    final int numberofObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double presentValueIndexPremiumLeg = 0.0;

    for (int i = 0; i < numberofObligors; i++) {

      final double pv = 0.0;

      presentValueIndexPremiumLeg += pv;
    }

    return presentValueIndexPremiumLeg;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the present value of an Index CDS contingent leg
  private double calculateIndexContingentLeg(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurves,
      final HazardRateCurve[] hazardRateCurves) {

    final int numberofObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    double presentValueIndexContingentLeg = 0.0;

    for (int i = 0; i < numberofObligors; i++) {

      final double pv = 0.0;

      presentValueIndexContingentLeg += pv;
    }

    return presentValueIndexContingentLeg;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
