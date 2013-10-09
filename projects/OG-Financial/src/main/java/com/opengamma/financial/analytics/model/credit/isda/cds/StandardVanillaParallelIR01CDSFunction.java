/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.CreditSpreadBumpersNew;
import com.opengamma.analytics.financial.credit.bumpers.InterestRateBumpers;
import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda.ISDACreditDefaultSwapParallelIR01Calculator;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.InterestRateSensitivityCalculator;
import com.opengamma.analytics.financial.credit.isdayieldcurve.InterestRateBumpType;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * 
 */
public class StandardVanillaParallelIR01CDSFunction extends StandardVanillaIR01CDSFunction {
  private static final InterestRateSensitivityCalculator CALCULATOR = new InterestRateSensitivityCalculator();

  public StandardVanillaParallelIR01CDSFunction() {
    super(ValueRequirementNames.IR01);
  }

  @Override
  protected Set<ComputedValue> getComputedValue(final CreditDefaultSwapDefinition definition,
                                                final ISDACompliantYieldCurve yieldCurve,
                                                final ZonedDateTime[] times,
                                                final double[] marketSpreads,
                                                final ZonedDateTime valuationDate,
                                                final ComputationTarget target,
                                                final ValueProperties properties,
                                                final FunctionInputs inputs,
                                                ISDACompliantCreditCurve hazardCurve, CDSAnalytic analytic) {
    final double ir01 = getParallelIR01(definition, yieldCurve, properties, hazardCurve, analytic);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.IR01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, ir01));
  }

  public static double getParallelIR01(CreditDefaultSwapDefinition definition,
                                 ISDACompliantYieldCurve yieldCurve,
                                 ValueProperties properties,
                                 ISDACompliantCreditCurve hazardCurve,
                                 CDSAnalytic analytic) {
    final Double interestRateCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(
        CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_CURVE_BUMP)));
    final InterestRateBumpType interestRateBumpType =
        InterestRateBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_INTEREST_RATE_BUMP_TYPE)));
    //final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));

    return interestRateCurveBump * definition.getNotional() * CALCULATOR.parallelIR01(analytic, getCoupon(definition), hazardCurve, yieldCurve);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> cdsPriceTypes = constraints.getValues(PROPERTY_CDS_PRICE_TYPE);
    if (cdsPriceTypes == null || cdsPriceTypes.size() != 1) {
      return null;
    }
    //final Set<String> hazardRateCurveCalculationMethodNames = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD);
    //if (hazardRateCurveCalculationMethodNames == null || hazardRateCurveCalculationMethodNames.size() != 1) {
    //  return null;
    //}
    return requirements;
  }

}
