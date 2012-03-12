/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Period;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.financial.sensitivities.FactorType;
import com.opengamma.financial.sensitivities.RawSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction.class);
  /**
   * The value name calculated by this function.
   */
  public static final String YCNS_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  private static final CharSequence SWAP_TEXT = "SWAP";

  @Override
  public void init(final FunctionCompilationContext context) {

  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getPosition().getSecurity() instanceof RawSecurity)) {
      return false;
    }
    final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    return security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE);
  }

  private ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties();
    properties.with(ValuePropertyNames.CURRENCY, currency);
    properties.with(ValuePropertyNames.CURVE_CURRENCY, currency);
    properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties(target);
    properties.withAny(ValuePropertyNames.CURVE);
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(2);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    results.add(new ValueSpecification(YCNS_REQUIREMENT, targetSpec, properties.get()));
    s_logger.debug("getResults(1) = " + results);
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    if ((curves == null) || (curves.size() != 1)) {
      s_logger.warn("no curve specified, just returning requirements for external sensitivities");
      // Can't support an unbound request; an injection function must be used (or declare all as optional and use [PLAT-1771])
      return null;
    }
    // TODO: if "CURVE" is specified, check that it is one of the forward/funding curve names
    final String curve = curves.iterator().next();
    final Set<ValueRequirement> requirements = Sets.newHashSet();
    requirements.add(getCurveRequirement(target, curve, curve, curve));
    requirements.add(getCurveSpecRequirement(target, curve));
    requirements.addAll(getSensitivityRequirements(context.getSecuritySource(), (RawSecurity) target.getPosition().getSecurity()));
    return requirements;
  }



  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(2);
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getKey().getValueName())) {
        assert curveName == null;
        curveName = input.getKey().getProperty(ValuePropertyNames.CURVE);
        assert curveName != null;
        final ValueProperties.Builder properties = createValueProperties(target);
        properties.with(ValuePropertyNames.CURVE, curveName);
        results.add(new ValueSpecification(YCNS_REQUIREMENT, targetSpec, properties.get()));
      }
    }
    s_logger.debug("getResults(2) returning " + results);
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    String curveName = null;
    for (final ValueRequirement requirement : desiredValues) {
      final ValueProperties constraints = requirement.getConstraints();
      final Set<String> values = constraints.getValues(ValuePropertyNames.CURVE);
      if (values != null) {
        curveName = values.iterator().next();
      }
    }
    assert curveName != null;
    final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    final ValueRequirement curveRequirement = getCurveRequirement(target, curveName, null, null);
    final Object curveObject = inputs.getValue(curveRequirement);
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveRequirement);
    }
    Object curveSpecObject = null;
    final ValueRequirement curveSpecRequirement = getCurveSpecRequirement(target, curveName);
    curveSpecObject = inputs.getValue(curveSpecRequirement);
    if (curveSpecObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveSpecRequirement);
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    final InterpolatedYieldCurveSpecificationWithSecurities curveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) curveSpecObject;
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(curveName, curve);
    final YieldCurveBundle bundle = new YieldCurveBundle(interpolatedCurves);
    final DoubleMatrix1D sensitivitiesForCurves = getSensitivities(executionContext.getSecuritySource(), inputs, security, curveSpec, curve);
    final ValueProperties.Builder properties = createValueProperties(target);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueSpecification resultSpec = new ValueSpecification(YCNS_REQUIREMENT, targetSpec, properties.with(ValuePropertyNames.CURVE, curveName).get());
    final Set<ComputedValue> results = YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(curveName, bundle, sensitivitiesForCurves, curveSpec, resultSpec);
    //s_logger.debug("execute, returning " + results);
    return results;
  }

  private DoubleMatrix1D getSensitivities(final SecuritySource secSource, final FunctionInputs inputs, final RawSecurity rawSecurity, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldAndDiscountCurve curve) {
    final Collection<FactorExposureData> decodedSensitivities = RawSecurityUtils.decodeFactorExposureData(secSource, rawSecurity);
    final double[] entries = new double[curveSpec.getStrips().size()];
    int i = 0;
    for (final FixedIncomeStripWithSecurity strip : curveSpec.getStrips()) {
      final FactorExposureData externalSensitivitiesData = searchForTenorMatch(decodedSensitivities, strip);
      if (externalSensitivitiesData != null) {
        final ComputedValue computedValue = inputs.getComputedValue(getSensitivityRequirement(externalSensitivitiesData.getExposureExternalId()));
        if (computedValue != null) {
          final Double value = (Double) computedValue.getValue();
          entries[i] = value;
        } else {
          s_logger.warn("Value was null when getting required input data " + externalSensitivitiesData.getExposureExternalId());
          entries[i] = 0d;
        }
      } else {
        entries[i] = 0d;
      }
      i++;
    }
    return new DoubleMatrix1D(entries);
  }

  private FactorExposureData searchForTenorMatch(final Collection<FactorExposureData> exposures, final FixedIncomeStripWithSecurity strip) {
    for (final FactorExposureData exposure : exposures) {
      if (exposure.getFactorType().equals(FactorType.YIELD) && exposure.getFactorName().contains(SWAP_TEXT)) {
        if (exposure.getNode() != null && exposure.getNode().length() > 0) {
          final Period nodePeriod = Period.parse("P" + exposure.getNode());
          if (strip.getTenor().getPeriod().totalMonths() == nodePeriod.totalMonths()) {
            return exposure;
          }
        }
      }
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction";
  }

  protected Set<ValueRequirement> getSensitivityRequirements(final SecuritySource secSource, final RawSecurity rawSecurity) {
    final Set<ValueRequirement> requirements = Sets.newHashSet();
    final Collection<FactorExposureData> decodedSensitivities = RawSecurityUtils.decodeFactorExposureData(secSource, rawSecurity);
    for (final FactorExposureData exposureEntry : decodedSensitivities) {
      requirements.add(getSensitivityRequirement(exposureEntry.getExposureExternalId()));
    }
    return requirements;
  }

  protected ValueRequirement getSensitivityRequirement(final ExternalId externalId) {
    return new ValueRequirement(/*ExternalDataRequirementNames.SENSITIVITY*/"EXPOSURE", ComputationTargetType.PRIMITIVE, UniqueId.of(externalId.getScheme().getName(), externalId.getValue()));
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForwardCurve, final String advisoryFundingCurve) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (advisoryForwardCurve != null) {
      properties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, advisoryForwardCurve);
    }
    if (advisoryFundingCurve != null) {
      properties.with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, advisoryFundingCurve);
    }
    properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  protected ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

}
