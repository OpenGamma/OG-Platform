/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves.inflationbondcurves;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.PV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PV01CurveParametersInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityIssuerDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *  Calculates the PV01 of a linked bond  for all curves to which the instruments are sensitive.
 */
public class InflationBondPV01Function extends InflationBondFromCurvesFunction<InflationIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InflationBondPV01Function.class);
  /** The PV01 calculator */
  private static final InstrumentDerivativeVisitor<InflationIssuerProviderInterface, ReferenceAmount<Pair<String, Currency>>> CALCULATOR =
      new PV01CurveParametersInflationCalculator<>(PresentValueCurveSensitivityIssuerDiscountingInflationCalculator.getInstance());

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#PV01} and
   * sets the calculator to {@link PV01CurveParametersCalculator}
   */
  public InflationBondPV01Function() {
    super(PV01, CALCULATOR);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext context, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(context.getValuationClock());
    final InstrumentDerivative derivative = BondAndBondFutureFunctionUtils.getBondOrBondFutureDerivative(context, target, now, inputs);
    final InflationIssuerProviderInterface issuerCurves = (InflationIssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
    final String desiredCurveName = properties.getStrictValue(CURVE);
    final ReferenceAmount<Pair<String, Currency>> pv01 = derivative.accept(CALCULATOR, issuerCurves);
    final Set<ComputedValue> results = new HashSet<>();
    boolean curveNameFound = false;
    for (final Map.Entry<Pair<String, Currency>, Double> entry : pv01.getMap().entrySet()) {
      final String curveName = entry.getKey().getFirst();
      if (desiredCurveName.equals(curveName)) {
        curveNameFound = true;
      }
      final ValueProperties curveSpecificProperties = properties.copy()
          .withoutAny(CURVE)
          .with(CURVE, curveName)
          .get();
      final ValueSpecification spec = new ValueSpecification(PV01, target.toSpecification(), curveSpecificProperties);
      results.add(new ComputedValue(spec, entry.getValue()));
    }
    if (!curveNameFound) {
      s_logger.error("Could not get sensitivities to " + desiredCurveName + " for " + target.getName());
      return Collections.emptySet();
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curveNames = desiredValue.getConstraints().getValues(CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    return super.getResultProperties(target)
        .with(CURRENCY, currency)
        .withAny(CURVE);
  }

}
