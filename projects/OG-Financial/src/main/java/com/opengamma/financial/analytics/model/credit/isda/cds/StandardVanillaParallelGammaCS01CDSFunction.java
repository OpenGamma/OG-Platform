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
import com.opengamma.analytics.financial.credit.bumpers.CreditSpreadBumpersNew;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
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
public class StandardVanillaParallelGammaCS01CDSFunction extends StandardVanillaCS01CDSFunction {
  private static final CreditSpreadBumpersNew SPREAD_BUMPER = new CreditSpreadBumpersNew();

  public StandardVanillaParallelGammaCS01CDSFunction() {
    super(ValueRequirementNames.GAMMA_CS01);
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
    final Double spreadCurveBump = Double.valueOf(Iterables.getOnlyElement(properties.getValues(
        CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP)));
    final SpreadBumpType spreadBumpType = SpreadBumpType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_BUMP_TYPE)));
    //TODO: Pass this down
    //final PriceType priceType = PriceType.valueOf(Iterables.getOnlyElement(properties.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)));
    final double gammaCS01 = parallelGammaCS01(definition, yieldCurve, hazardCurve, analytic, spreadCurveBump, spreadBumpType);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.GAMMA_CS01, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, gammaCS01));
  }

  public static double parallelGammaCS01(final CreditDefaultSwapDefinition definition, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve hazardCurve,
                                   final CDSAnalytic analytic, final double spreadCurveBump, final SpreadBumpType spreadBumpType) {
    final double[] rates = hazardCurve.getKnotZeroRates();
    final double[] bumpedUpRates = SPREAD_BUMPER.getBumpedCreditSpreads(rates, spreadCurveBump * 1e-4, spreadBumpType);
    final double[] bumpedDownRates = SPREAD_BUMPER.getBumpedCreditSpreads(rates, -spreadCurveBump * 1e-4, spreadBumpType);
    final ISDACompliantCreditCurve bumpedUpCreditCurve = hazardCurve.withRates(bumpedUpRates);
    final ISDACompliantCreditCurve bumpedDownCreditCurve = hazardCurve.withRates(bumpedDownRates);
    final double pv = StandardVanillaPresentValueCDSFunction.presentValue(definition, yieldCurve, hazardCurve, analytic);
    final double bumpedUpPresentValue = StandardVanillaPresentValueCDSFunction.presentValue(definition, yieldCurve, bumpedUpCreditCurve, analytic);
    final double bumpedDownPresentValue = StandardVanillaPresentValueCDSFunction.presentValue(definition, yieldCurve, bumpedDownCreditCurve, analytic);
    return (bumpedUpPresentValue - 2 * pv + bumpedDownPresentValue) / (spreadCurveBump * spreadCurveBump);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> bumptype = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_BUMP_TYPE);
    if (bumptype == null || bumptype.size() != 1) {
      return null;
    }
    final Set<String> bump = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP);
    if (bump == null || bump.size() != 1) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String spreadCurveName = security.accept(new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(
        context))).getUniqueId().getValue();
    //TODO shouldn't need all of the yield curve properties
    final String hazardRateCurveCalculationMethod = "ISDA"; //Iterables.getOnlyElement(hazardRateCurveCalculationMethodNames);
    final String yieldCurveName = desiredValue.getConstraint(PROPERTY_YIELD_CURVE);
    final String yieldCurveCalculationConfig = desiredValue.getConstraint(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
    final String yieldCurveCalculationMethod = desiredValue.getConstraint(PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
    final Set<String> creditSpreadCurveShifts = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT);
    final Set<String> creditSpreadCurveShiftTypes = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT_TYPE);
    final ValueProperties.Builder hazardRateCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, spreadCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, hazardRateCurveCalculationMethod)
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

}
