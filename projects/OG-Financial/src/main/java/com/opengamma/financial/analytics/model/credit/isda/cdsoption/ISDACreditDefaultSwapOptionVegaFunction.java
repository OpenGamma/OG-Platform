/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdsoption;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.bumpers.SpreadVolatilityBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.greeks.VegaCreditDefaultSwapOption;
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
public class ISDACreditDefaultSwapOptionVegaFunction extends ISDACreditDefaultSwapOptionFunction {
  private static final VegaCreditDefaultSwapOption CALCULATOR = new VegaCreditDefaultSwapOption();

  public ISDACreditDefaultSwapOptionVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapOptionDefinition definition, final ISDADateCurve yieldCurve, final double vol,
      final ZonedDateTime[] calibrationTenors, final double[] marketSpreads, final HazardRateCurve hazardRateCurve, final ZonedDateTime valuationTime,
      final ComputationTarget target, final ValueProperties properties) {
    final double vega = CALCULATOR.getVegaCreditDefaultSwapOption(valuationTime, definition, vol, yieldCurve, hazardRateCurve, calibrationTenors, marketSpreads, 0.01,
        SpreadVolatilityBumpType.MULTIPLICATIVE);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VALUE_VEGA, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, vega));
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
