/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdsoption;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing.PresentValueCreditDefaultSwapOption;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ISDACreditDefaultSwapOptionPVFunction extends ISDACreditDefaultSwapOptionFunction {
  private static final PresentValueCreditDefaultSwapOption CALCULATOR = new PresentValueCreditDefaultSwapOption();

  public ISDACreditDefaultSwapOptionPVFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapOptionDefinition definition, final ISDADateCurve yieldCurve, final double vol,
      final ZonedDateTime[] calibrationTenors, final double[] marketSpreads, final HazardRateCurve hazardRateCurve, final ZonedDateTime valuationTime,
      final ComputationTarget target, final ValueProperties properties) {
    final double pv = CALCULATOR.getPresentValueCreditDefaultSwapOption(valuationTime, definition, vol, calibrationTenors, marketSpreads, yieldCurve, hazardRateCurve);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

  @Override
  protected boolean labelResultWithCurrency() {
    return true;
  }

  @Override
  protected Builder getCommonResultProperties() {
    return createValueProperties();
  }
}
