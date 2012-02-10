/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Period;

import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.ExternalDataRequirementNames;
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
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldAndDiscountCurveFunction;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction.class);
  /**
   * The value name calculated by this function.
   */
  public static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;

  private static final String RESULT_PROPERTY_TYPE = "Type";
  private static final String TYPE_FORWARD = "Forward";
  private static final String TYPE_FUNDING = "Funding";

  @Override
  public void init(final FunctionCompilationContext context) {

  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!(target.getSecurity() instanceof RawSecurity)) {
      return false;
    }
    final RawSecurity security = (RawSecurity) target.getSecurity();
    return security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE);
  }

  private ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final Security security = (Security) target.getSecurity();
    final ValueProperties.Builder properties = createValueProperties();
    FixedIncomeInstrumentCurveExposureHelper.valuePropertiesForSecurity(security, properties);
    properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, InterpolatedYieldAndDiscountCurveFunction.INTERPOLATED_CALCULATION_METHOD);
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties(target);
    properties.withAny(ValuePropertyNames.CURVE);
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(2);
    properties.with(RESULT_PROPERTY_TYPE, TYPE_FORWARD);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    results.add(new ValueSpecification(VALUE_REQUIREMENT, targetSpec, properties.get()));
    properties.withoutAny(RESULT_PROPERTY_TYPE).with(RESULT_PROPERTY_TYPE, TYPE_FUNDING);
    results.add(new ValueSpecification(VALUE_REQUIREMENT, targetSpec, properties.get()));
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    
    if ((curves == null) || (curves.size() != 1)) {
      // Can't support an unbound request; an injection function must be used (or declare all as optional and use [PLAT-1771])
      return null;
    }
    // TODO: if "CURVE" is specified, check that it is one of the forward/funding curve names
    final String curve = curves.iterator().next();
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(6);
    
    requirements.add(getCurveRequirement(target, curve, curve, curve));
    requirements.add(getCurveSpecRequirement(target, curve));
    
    requirements.addAll(getSensitivityRequirements(context.getSecuritySource(), (RawSecurity) target.getSecurity()));

    return requirements;
  }
  
  protected Set<ValueRequirement> getSensitivityRequirements(SecuritySource secSource, RawSecurity rawSecurity) {
    Set<ValueRequirement> requirements = Sets.newHashSet();
    Collection<FactorExposureData> decodedSensitivities = decodeSensitivities(secSource, rawSecurity);
    for (FactorExposureData exposureEntry : decodedSensitivities) {
      requirements.add(getSensitivityRequirement(exposureEntry.getExposureExternalId()));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    for (Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES.equals(input.getKey().getValueName())) {
        assert curveName == null;
        curveName = input.getKey().getProperty(ValuePropertyNames.CURVE);
      }
    }
    assert curveName != null;
    final ValueProperties.Builder properties = createValueProperties(target);
    properties.with(ValuePropertyNames.CURVE, curveName);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Set<ValueSpecification> result;
    result = Collections.singleton(new ValueSpecification(VALUE_REQUIREMENT, targetSpec, properties.get()));
    return result;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueProperties constraints = desiredValues.iterator().next().getConstraints();
    final String curveName = constraints.getValues(ValuePropertyNames.CURVE).iterator().next();
    final RawSecurity security = (RawSecurity) target.getSecurity();
//    final Clock snapshotClock = executionContext.getValuationClock();
//    final ZonedDateTime now = snapshotClock.zonedDateTime();
//    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
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
    DoubleMatrix1D sensitivitiesForCurves = getSensitivities(executionContext.getSecuritySource(), inputs, security, curveSpec, curve);
    final ValueProperties.Builder properties = createValueProperties(target);
    properties.with(ValuePropertyNames.CURVE, curveName);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueSpecification resultSpec = new ValueSpecification(VALUE_REQUIREMENT, targetSpec, properties.with(ValuePropertyNames.CURVE, curveName).get());
    return YieldCurveNodeSensitivitiesHelper.getSensitivitiesForCurve(curveName, bundle, sensitivitiesForCurves, curveSpec, resultSpec);
  }
  
  private DoubleMatrix1D getSensitivities(SecuritySource secSource, FunctionInputs inputs, RawSecurity rawSecurity, InterpolatedYieldCurveSpecificationWithSecurities curveSpec, 
                                          YieldAndDiscountCurve curve) {
    Collection<FactorExposureData> decodedSensitivities = decodeSensitivities(secSource, rawSecurity);
    double[] entries = new double[curveSpec.getStrips().size()];
    int i = 0;
    for (FixedIncomeStripWithSecurity strip : curveSpec.getStrips()) {
      FactorExposureData externalSensitivitiesData = searchForTenorMatch(decodedSensitivities, strip);
      if (externalSensitivitiesData != null) {
        ComputedValue computedValue = inputs.getComputedValue(getSensitivityRequirement(externalSensitivitiesData.getExposureExternalId()));
        if (computedValue != null) {
          Double value = (Double) computedValue.getValue();
          entries[i] = value;
        } else {
          s_logger.error("Value was null when getting required input data " + externalSensitivitiesData.getExposureExternalId());
          entries[i] = 0d;
        }
      } else {
        entries[i] = 0d;
      }
      i++;
    }
    return new DoubleMatrix1D(entries);
  }
  
  
  
  private FactorExposureData searchForTenorMatch(Collection<FactorExposureData> exposures, FixedIncomeStripWithSecurity strip) {
    for (FactorExposureData exposure : exposures) {
      if (exposure.getNode() != null) {
        Period nodePeriod = Period.parse("P" + exposure.getNode());
        if (strip.getTenor().getPeriod().totalMonths() == nodePeriod.totalMonths()) {
          return exposure;
        }
      }
    }
    return null;
  }
  
  private Collection<FactorExposureData> decodeSensitivities(SecuritySource secSource, RawSecurity rawSecurity) {
    FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
    SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
    RawSecurity underlyingRawSecurity = (RawSecurity) secSource.getSecurity(securityEntryData.getFactorSetId().toBundle());
    if (underlyingRawSecurity != null) {
      FudgeMsgEnvelope factorIdMsg = OpenGammaFudgeContext.getInstance().deserialize(underlyingRawSecurity.getRawData());
      @SuppressWarnings("unchecked")
      List<FactorExposureData> factorExposureDataList = OpenGammaFudgeContext.getInstance().fromFudgeMsg(List.class, factorIdMsg.getMessage());
      //s_logger.error(factorExposureDataList.toString());
      return factorExposureDataList;
    } else {
      throw new OpenGammaRuntimeException("Couldn't find factor list security " + securityEntryData.getFactorSetId());
    }
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentYieldCurveNodeSensitivitiesFunction";
  }
  
  protected ValueRequirement getSensitivityRequirement(final ExternalId externalId) {
    return new ValueRequirement(ExternalDataRequirementNames.SENSITIVITY, ComputationTargetType.PRIMITIVE, UniqueId.of(externalId.getScheme().getName(), externalId.getValue()));
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForwardCurve, final String advisoryFundingCurve) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    if (advisoryForwardCurve != null) {
      properties.with(YieldCurveFunction.PROPERTY_FORWARD_CURVE, advisoryForwardCurve);
    }
    if (advisoryFundingCurve != null) {
      properties.with(YieldCurveFunction.PROPERTY_FUNDING_CURVE, advisoryFundingCurve);
    }
    properties.with(ValuePropertyNames.CURVE_CALCULATION_METHOD, InterpolatedYieldAndDiscountCurveFunction.INTERPOLATED_CALCULATION_METHOD);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

  protected ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties.get());
  }

}
