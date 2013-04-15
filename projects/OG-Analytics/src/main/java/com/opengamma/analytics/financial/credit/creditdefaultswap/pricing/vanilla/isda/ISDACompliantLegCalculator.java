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

  public abstract double calculateLeg(ZonedDateTime valuationDate, CreditDefaultSwapDefinition cds, ISDAYieldCurveAndHazardRateCurveProvider curves,
      PriceType priceType);
}
