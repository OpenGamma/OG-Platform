/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cds;

import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSRiskFactors;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
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
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class StandardVanillaRR01CDSFunction extends StandardVanillaCDSFunction {
  private static final CDSRiskFactors CALCULATOR = new CDSRiskFactors();

  public StandardVanillaRR01CDSFunction() {
    super(ValueRequirementNames.RR01);
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
                                                final ISDACompliantCreditCurve hazardCurve, final CDSAnalytic analytic, final Tenor[] tenors) {

    final double rr01 = getRR01(definition, yieldCurve, properties, hazardCurve, analytic);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.RR01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, rr01));
  }

  public static double getRR01(final CreditDefaultSwapDefinition definition,
                         final ISDACompliantYieldCurve yieldCurve,
                         final ValueProperties properties, final ISDACompliantCreditCurve hazardCurve, final CDSAnalytic analytic) {
    final RecoveryRateBumpType recoveryRateBumpType =
        RecoveryRateBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(
            CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_BUMP_TYPE)));
    if (recoveryRateBumpType != RecoveryRateBumpType.ADDITIVE) {
      throw new UnsupportedOperationException("Only Additive rr01 sensitivity supported currently. Got " + recoveryRateBumpType);
    }
    final Double bump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(
        CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_CURVE_BUMP)));
    return bump * 1e-4 * definition.getNotional() * CALCULATOR.recoveryRateSensitivity(analytic,
                                                                                       yieldCurve,
                                                                                       hazardCurve);
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
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String spreadCurveName = security.accept(new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(
        context))).getUniqueId().getValue();
    //TODO shouldn't need all of the yield curve properties
    final String yieldCurveName = desiredValue.getConstraint(PROPERTY_YIELD_CURVE);
    final String yieldCurveCalculationConfig = desiredValue.getConstraint(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
    final String yieldCurveCalculationMethod = desiredValue.getConstraint(PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
    final Set<String> creditSpreadCurveShifts = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT);
    final Set<String> creditSpreadCurveShiftTypes = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT_TYPE);
    final ValueProperties.Builder hazardRateCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, spreadCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, "ISDA")
        .with(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG, yieldCurveCalculationConfig)
        .with(PROPERTY_YIELD_CURVE_CALCULATION_METHOD, yieldCurveCalculationMethod)
        .with(PROPERTY_YIELD_CURVE, yieldCurveName);
    if (creditSpreadCurveShifts != null) {
      hazardRateCurveProperties.with(PROPERTY_SPREAD_CURVE_SHIFT, creditSpreadCurveShifts).with(PROPERTY_SPREAD_CURVE_SHIFT_TYPE, creditSpreadCurveShiftTypes);
    }
    final ValueRequirement hazardRateCurveRequirement = new ValueRequirement(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), hazardRateCurveProperties.get());
    requirements.add(hazardRateCurveRequirement);
    return requirements;
  }

  @Override
  protected ValueProperties.Builder getCommonResultProperties() {
    return createValueProperties()
        .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_CURVE_BUMP)
        .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_RECOVERY_RATE_BUMP_TYPE);
  }

  @Override
  protected boolean labelResultWithCurrency() {
    return true;
  }
}
