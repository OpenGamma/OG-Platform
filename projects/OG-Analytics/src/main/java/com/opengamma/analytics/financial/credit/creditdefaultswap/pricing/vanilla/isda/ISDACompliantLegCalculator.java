/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;

/**
 * 
 */
public abstract class ISDACompliantLegCalculator {

  /**
   * get the PV of a leg of a CDS 
   * @param valuationDate The date that all cash-flows are PVed back to
   * @param cds Description of the CDS 
   * @param curves Container of (ISDA model compliant) discount (yield) and survival (hazard rate) curves    
   * @param priceType Clean or dirty 
   * @return The PV of the leg
   */
  public abstract double calculateLeg(ZonedDateTime valuationDate, CreditDefaultSwapDefinition cds, ISDAYieldCurveAndHazardRateCurveProvider curves,
      PriceType priceType);
}
