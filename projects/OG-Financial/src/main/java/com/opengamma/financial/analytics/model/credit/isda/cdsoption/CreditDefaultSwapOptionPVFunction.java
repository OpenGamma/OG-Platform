/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdsoption;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing.PresentValueCreditDefaultSwapOption;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;

/**
 * 
 */
public class CreditDefaultSwapOptionPVFunction extends CreditDefaultSwapOptionFunction {
  private static final PresentValueCreditDefaultSwapOption CALCULATOR = new PresentValueCreditDefaultSwapOption();

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapOptionDefinition definition, final ISDADateCurve yieldCurve, final double vol,
      final HazardRateCurve hazardRateCurve, final ZonedDateTime valuationTime, final ComputationTarget target, final ValueProperties properties) {
    //final double pv = CALCULATOR.getPresentValueCreditDefaultSwapOption(valuationTime, definition, vol, yieldCurve, hazardRateCurve);
    return null;
  }
}
