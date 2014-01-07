/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondyield;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.YIELD_CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.Z_SPREAD;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondSecurityDiscountingMethod;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProvider;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;


/**
 * Calculates the z-spread of a bond from the clean price.
 */
public class BondZSpreadFromYieldFunction extends BondFromYieldAndCurvesFunction {
  /** The z-spread calculator */
  private static final BondSecurityDiscountingMethod CALCULATOR = BondSecurityDiscountingMethod.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#Z_SPREAD}
   */
  public BondZSpreadFromYieldFunction() {
    super(Z_SPREAD);
  }

  @Override
  protected Set<ComputedValue> getResult(final FunctionInputs inputs, final BondFixedTransaction bond, final IssuerProvider issuerCurves,
      final double cleanPrice, final ValueSpecification spec) {
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) inputs.getValue(YIELD_CURVE);
    final LegalEntity legalEntity = bond.getBondTransaction().getIssuerEntity();
    final Set<Pair<Object, LegalEntityFilter<LegalEntity>>> keys = issuerCurves.getIssuers();
    Pair<Object, LegalEntityFilter<LegalEntity>> keyOfCurveToReplace = null;
    for (final Pair<Object, LegalEntityFilter<LegalEntity>> key : keys) {
      if (key.getFirst().equals(key.getSecond().getFilteredData(legalEntity))) {
        keyOfCurveToReplace = key;
        break;
      }
    }
    if (keyOfCurveToReplace == null) {
      throw new OpenGammaRuntimeException("Could not find key for " + legalEntity);
    }
    final IssuerProvider curvesWithReplacement = issuerCurves.withIssuerCurve(keyOfCurveToReplace, curve);
    final double zSpread = 10000 * CALCULATOR.zSpreadFromCurvesAndYield(bond.getBondTransaction(), curvesWithReplacement, cleanPrice);
    return Collections.singleton(new ComputedValue(spec, zSpread));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curves = constraints.getValues(CURVE);
    if (curves == null || curves.size() != 1) {
      return null;
    }
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final String curve = Iterables.getOnlyElement(curves);
    final ValueProperties curveProperties = ValueProperties.builder()
        .with(CURVE, curve)
        .with(PROPERTY_CURVE_TYPE, constraints.getValues(PROPERTY_CURVE_TYPE))
        .get();
    requirements.add(new ValueRequirement(YIELD_CURVE, ComputationTargetSpecification.NULL, curveProperties));
    return requirements;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return super.getResultProperties(target)
        .withAny(CURVE);
  }
}
