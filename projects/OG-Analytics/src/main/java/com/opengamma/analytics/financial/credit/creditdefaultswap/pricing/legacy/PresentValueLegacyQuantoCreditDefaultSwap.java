/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyQuantoCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the valuation of a Legacy quanto CDS
 */
public class PresentValueLegacyQuantoCreditDefaultSwap {

  //----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to modify the pricer to take into account the quanto nature of the contract

  // NOTE : For most types of legacy CDS, the same underlying pricing model will be used
  // NOTE : However, if required we can override these methods to provide bespoke pricing models
  // NOTE : for specific types of CDS if required

  //----------------------------------------------------------------------------------------------------------------------------------------

  // Create a PV calculator for a legacy CDS object
  private static final PresentValueLegacyCreditDefaultSwap presentValueLegacyCreditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract (with a hazard rate curve calibrated to market observed data)

  public double getPresentValueLegacyQuantoCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyQuantoCreditDefaultSwapDefinition quantoCDS,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(quantoCDS, "LegacyQuantoCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve");
    ArgumentChecker.notNull(priceType, "price type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the CDS PV
    final double quantoCDSPresentValue = presentValueLegacyCreditDefaultSwap.getPresentValueLegacyCreditDefaultSwap(valuationDate, quantoCDS, yieldCurve, hazardRateCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return quantoCDSPresentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method to calculate the par spread of a CDS at contract inception (with a hazard rate curve calibrated to market observed data)

  public double getParSpreadLegacyQuantoCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyQuantoCreditDefaultSwapDefinition quantoCDS,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(quantoCDS, "LegacyQuantoCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve");
    ArgumentChecker.notNull(priceType, "price type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the CDS par spread
    final double quantoCDSParSpread = presentValueLegacyCreditDefaultSwap.getParSpreadLegacyCreditDefaultSwap(valuationDate, quantoCDS, yieldCurve, hazardRateCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return quantoCDSParSpread;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
