/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.collections.Lists;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.financial.sensitivities.FactorExposureDataComparator;
import com.opengamma.financial.sensitivities.FactorType;
import com.opengamma.financial.sensitivities.RawSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ExternallyProvidedSensitivitiesNonYieldCurveFunction extends AbstractFunction.NonCompiledInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(ExternallyProvidedSensitivitiesNonYieldCurveFunction.class);
  /**
   * The value name calculated by this function.
   */
  public static final String EXTERNAL_SENSITIVITIES_REQUIREMENT = ValueRequirementNames.EXTERNAL_SENSITIVITIES;
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

  private ValueProperties.Builder createCurrencyValueProperties(final ComputationTarget target) {
    final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    SecurityEntryData securityEntryData = RawSecurityUtils.decodeSecurityEntryData(security);
    final Currency ccy = securityEntryData.getCurrency();
    final ValueProperties.Builder properties = createValueProperties();
    properties.with(ValuePropertyNames.CURRENCY, ccy.getCode());
    return properties;    
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder externalProperties = createCurrencyValueProperties(target);
    externalProperties.withAny(ValuePropertyNames.CURRENCY);
    Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(EXTERNAL_SENSITIVITIES_REQUIREMENT, target.toSpecification(), externalProperties.get()));
    s_logger.warn("getResults(1) = " + results);
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = getSensitivityRequirements(context.getSecuritySource(), (RawSecurity) target.getPosition().getSecurity());
    //s_logger.warn("getRequirements() returned " + requirements);
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
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    Set<ValueSpecification> results = Collections.singleton(new ValueSpecification(EXTERNAL_SENSITIVITIES_REQUIREMENT, targetSpec, createCurrencyValueProperties(target).get()));
    //s_logger.warn("getResults(2) returning " + results);
    return results;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
                                    final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final RawSecurity security = (RawSecurity) target.getPosition().getSecurity();
    Set<ComputedValue> results = getResultsForExternalRiskFactors(executionContext.getSecuritySource(), inputs, target, security);
    //s_logger.warn("execute, returning " + results);
    return results;
  }
  
  private Set<ComputedValue> getResultsForExternalRiskFactors(SecuritySource secSource, FunctionInputs inputs, ComputationTarget target, RawSecurity security) {
    List<FactorExposureData> factors = decodeSensitivities(secSource, security);
    Collections.sort(factors, new FactorExposureDataComparator());
    List<String> indices = Lists.newArrayList();
    List<String> labels = Lists.newArrayList();
    DoubleList values = new DoubleArrayList();
    for (FactorExposureData factor : factors) {
      if (!(factor.getFactorType().equals(FactorType.YIELD) && factor.getFactorName().contains(SWAP_TEXT))) {
        ComputedValue computedValue = inputs.getComputedValue(getSensitivityRequirement(factor.getExposureExternalId()));
        if (computedValue != null) {
          indices.add(factor.getFactorExternalId().getValue());
          labels.add(factor.getExposureExternalId().getValue());
          values.add((Double) computedValue.getValue());
        } else {
          s_logger.error("Value was null when getting required input data " + factor.getExposureExternalId());
        }
      }
    }
    StringLabelledMatrix1D labelledMatrix = new StringLabelledMatrix1D(indices.toArray(new String[] {}), labels.toArray(), values.toDoubleArray());
    ValueSpecification valueSpecification = new ValueSpecification(EXTERNAL_SENSITIVITIES_REQUIREMENT, target.toSpecification(), createCurrencyValueProperties(target).get());
    return Collections.singleton(new ComputedValue(valueSpecification, labelledMatrix));
  }
    
  private List<FactorExposureData> decodeSensitivities(SecuritySource secSource, RawSecurity rawSecurity) {
    FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
    SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
    RawSecurity underlyingRawSecurity = (RawSecurity) secSource.getSecurity(securityEntryData.getFactorSetId().toBundle());
    if (underlyingRawSecurity != null) {
      FudgeMsgEnvelope factorIdMsg = OpenGammaFudgeContext.getInstance().deserialize(underlyingRawSecurity.getRawData());
      @SuppressWarnings("unchecked")
      List<FactorExposureData> factorExposureDataList = OpenGammaFudgeContext.getInstance().fromFudgeMsg(List.class, factorIdMsg.getMessage());     
      return factorExposureDataList;
    } else {
      throw new OpenGammaRuntimeException("Couldn't find factor list security " + securityEntryData.getFactorSetId());
    }
  }

  @Override
  public String getShortName() {
    return "ExternallyProvidedSensitivitiesNonYieldCurveFunction";
  }
  
  protected ValueRequirement getSensitivityRequirement(final ExternalId externalId) {
    return new ValueRequirement(/*ExternalDataRequirementNames.SENSITIVITY*/"EXPOSURE", ComputationTargetType.PRIMITIVE, UniqueId.of(externalId.getScheme().getName(), externalId.getValue()));
  }

}
