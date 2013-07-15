/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda.ISDACreditDefaultSwapRR01Calculator;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;

/**
 * 
 */
public class StandardVanillaRR01CDSFunction extends StandardVanillaCDSFunction {
  private static final ISDACreditDefaultSwapRR01Calculator CALCULATOR = new ISDACreditDefaultSwapRR01Calculator();

  public StandardVanillaRR01CDSFunction() {
    super(ValueRequirementNames.RR01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition, final ISDADateCurve yieldCurve, final ZonedDateTime[] times,
      final double[] marketSpreads, final ZonedDateTime valuationDate, final ComputationTarget target, final ValueProperties properties,
      final FunctionInputs inputs) {
    final Double recoveryRateCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_CURVE_BUMP)));
    final RecoveryRateBumpType recoveryRateBumpType =
        RecoveryRateBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_BUMP_TYPE)));
    final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));
    final double rr01 = CALCULATOR.getRecoveryRate01CreditDefaultSwap(valuationDate, definition, yieldCurve, times, marketSpreads, recoveryRateCurveBump,
        recoveryRateBumpType, priceType);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.RR01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, rr01));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> recoveryRateBumps = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_CURVE_BUMP);
    if (recoveryRateBumps == null || recoveryRateBumps.size() != 1) {
      return null;
    }
    final Set<String> recoveryRateBumpTypes = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_BUMP_TYPE);
    if (recoveryRateBumpTypes == null || recoveryRateBumpTypes.size() != 1) {
      return null;
    }
    final Set<String> cdsPriceTypes = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE);
    if (cdsPriceTypes == null || cdsPriceTypes.size() != 1) {
      return null;
    }
    return requirements;
  }

  @Override
  protected ValueProperties.Builder getCommonResultProperties() {
    return createValueProperties()
        .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_CURVE_BUMP)
        .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_BUMP_TYPE)
        .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE);
  }

  @Override
  protected boolean labelResultWithCurrency() {
    return true;
  }
}
