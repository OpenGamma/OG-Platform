/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.calibratehazardratecurve.legacy;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;

/**
 * 
 */
public class CalibrateHazardRateCurveLegacyVanillaCreditDefaultSwap {

  CalibrateHazardRateCurveLegacyCreditDefaultSwap calibrateLegacyCDS = new CalibrateHazardRateCurveLegacyCreditDefaultSwap();

  public double[] getCalibratedHazardRateCurveLegacyVanillaCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition vanillaCDS,
      final ISDACurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    double[] h = new double[2];

    return h;

  }

}
