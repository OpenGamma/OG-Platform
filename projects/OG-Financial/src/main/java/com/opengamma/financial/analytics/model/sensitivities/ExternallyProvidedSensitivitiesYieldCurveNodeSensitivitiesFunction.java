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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.model.YieldCurveNodeSensitivitiesHelper;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.sensitivities.FactorExposureData;
import com.opengamma.financial.sensitivities.FactorType;
import com.opengamma.financial.sensitivities.RawSecurityUtils;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.security.RawSecurity;
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
  private static final CharSequence BOND_TEXT = "BOND";
  private HistoricalTimeSeriesResolver _htsResolver;

  @Override
  public void init(final FunctionCompilationContext context) {
    // REVIEW: jim 24-Oct-2012 -- this is a terrible, terrible hack.  Blame Andrew Griffin - he told me to do it.
    _htsResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.RAW_SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final RawSecurity security = (RawSecurity) target.getSecurity();
    return security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE);
  }

  private ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    final Security security = target.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties.Builder properties = createValueProperties();
    properties.with(ValuePropertyNames.CURRENCY, currency);
    properties.with(ValuePropertyNames.CURVE_CURRENCY, currency);
    return properties;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = createValueProperties(target);
    properties.withAny(ValuePropertyNames.CURVE);
    properties.withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(2);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    results.add(new ValueSpecification(YCNS_REQUIREMENT, targetSpec, properties.get()));
    s_logger.debug("getResults(1) = " + results);
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> curves = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE);
    final Set<String> curveCalcConfigs = desiredValue.getConstraints().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    if ((curves == null) || (curves.size() != 1)) {
      s_logger.warn("no curve specified");
      // Can't support an unbound request; an injection function must be used (or declare all as optional and use [PLAT-1771])
      return null;
    }
    if ((curveCalcConfigs == null) || (curveCalcConfigs.size() != 1)) {
      s_logger.warn("no curve config specified");
      return null;
    }
    final String curve = curves.iterator().next();
    final String curveCalcConfig = curveCalcConfigs.iterator().next();
    final Set<ValueRequirement> requirements = Sets.newHashSet();
    requirements.add(getCurveRequirement(target, curve, curveCalcConfig));
    requirements.add(getCurveSpecRequirement(target, curve));
    requirements.addAll(getSensitivityRequirements(context.getSecuritySource(), (RawSecurity) target.getSecurity()));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String curveName = null;
    String curveCalculationConfig = null;
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(2);
    for (final Map.Entry<ValueSpecification, ValueRequirement> input : inputs.entrySet()) {
      if (ValueRequirementNames.YIELD_CURVE.equals(input.getKey().getValueName())) {
        assert curveName == null;
        assert curveCalculationConfig == null;
        curveName = input.getKey().getProperty(ValuePropertyNames.CURVE);
        curveCalculationConfig = input.getKey().getProperty(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        assert curveName != null;
        assert curveCalculationConfig != null;
        final ValueProperties.Builder properties = createValueProperties(target);
        properties.with(ValuePropertyNames.CURVE, curveName);
        properties.with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig);
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
    String curveCalculationConfig = null;
    for (final ValueRequirement requirement : desiredValues) {
      final ValueProperties constraints = requirement.getConstraints();
      final Set<String> values = constraints.getValues(ValuePropertyNames.CURVE);
      if (values != null) {
        curveName = values.iterator().next();
      }
      final Set<String> curveConfigValues = constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      if (curveConfigValues != null) {
        curveCalculationConfig = curveConfigValues.iterator().next();
      }
    }
    assert curveName != null;
    assert curveCalculationConfig != null;
    final RawSecurity security = (RawSecurity) target.getSecurity();
    //final BigDecimal qty = target.getPosition().getQuantity();
    final ValueRequirement curveRequirement = getCurveRequirement(target, curveName, curveCalculationConfig);
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
    final ValueProperties.Builder properties = createValueProperties(target)
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueSpecification resultSpec = new ValueSpecification(YCNS_REQUIREMENT, targetSpec, properties.get());
    final Set<ComputedValue> results = YieldCurveNodeSensitivitiesHelper.getInstrumentLabelledSensitivitiesForCurve(curveName, bundle, sensitivitiesForCurves, curveSpec, resultSpec);
    //s_logger.debug("execute, returning " + results);
    return results;
  }

  private DoubleMatrix1D getSensitivities(final SecuritySource secSource, final FunctionInputs inputs, final RawSecurity rawSecurity,
      final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldAndDiscountCurve curve) {
    final Collection<FactorExposureData> decodedSensitivities = RawSecurityUtils.decodeFactorExposureData(secSource, rawSecurity);
    final double[] entries = new double[curveSpec.getStrips().size()];
    int i = 0;
    for (final FixedIncomeStripWithSecurity strip : curveSpec.getStrips()) {
      final FactorExposureData swapExternalSensitivitiesData = searchForSwapTenorMatch(decodedSensitivities, strip);
      if (swapExternalSensitivitiesData != null) {
        final ComputedValue computedValue = inputs.getComputedValue(getSensitivityRequirement(swapExternalSensitivitiesData.getExposureExternalId()));
        if (computedValue != null) {
          final ManageableHistoricalTimeSeries mhts = (ManageableHistoricalTimeSeries) computedValue.getValue();
          final Double value = mhts.getTimeSeries().getLatestValue();
          entries[i] = -value; //* (qty.doubleValue() ); // we invert here because OpenGamma uses -1bp shift rather than +1.  DV01 function will invert back.
        } else {
          s_logger.warn("Value was null when getting required input data " + swapExternalSensitivitiesData.getExposureExternalId());
          entries[i] = 0d;
        }
      } else {
        entries[i] = 0d;
      }
      i++;
    }
    // Quick hack to map in bond data.
    i = 0;
    for (final FixedIncomeStripWithSecurity strip : curveSpec.getStrips()) {
      final FactorExposureData bondExternalSensitivitiesData = searchForBondTenorMatch(decodedSensitivities, strip);
      if (bondExternalSensitivitiesData != null) {
        final ComputedValue computedValue = inputs.getComputedValue(getSensitivityRequirement(bondExternalSensitivitiesData.getExposureExternalId()));
        if (computedValue != null) {
          final ManageableHistoricalTimeSeries mhts = (ManageableHistoricalTimeSeries) computedValue.getValue();
          final Double value = mhts.getTimeSeries().getLatestValue();
          entries[i] -= value; //* (qty.doubleValue() ); // we invert here because OpenGamma uses -1bp shift rather than +1.  DV01 function will invert back.
        } else {
          s_logger.warn("Value was null when getting required input data " + bondExternalSensitivitiesData.getExposureExternalId());
        }
      }
      i++;
    }
    return new DoubleMatrix1D(entries);
  }

  private FactorExposureData searchForSwapTenorMatch(final Collection<FactorExposureData> exposures, final FixedIncomeStripWithSecurity strip) {
    for (final FactorExposureData exposure : exposures) {
      if (exposure.getFactorType().equals(FactorType.YIELD) && exposure.getFactorName().contains(SWAP_TEXT)) {
        if (exposure.getNode() != null && exposure.getNode().length() > 0) {
          final Period nodePeriod = Period.parse("P" + exposure.getNode());
          if (strip.getTenor().getPeriod().toTotalMonths() == nodePeriod.toTotalMonths()) {
            return exposure;
          }
        }
      }
    }
    return null;
  }

  private FactorExposureData searchForBondTenorMatch(final Collection<FactorExposureData> exposures, final FixedIncomeStripWithSecurity strip) {
    for (final FactorExposureData exposure : exposures) {
      if (exposure.getFactorType().equals(FactorType.YIELD) && exposure.getFactorName().contains(BOND_TEXT)) {
        if (exposure.getNode() != null && exposure.getNode().length() > 0) {
          final Period nodePeriod = Period.parse("P" + exposure.getNode());
          if (strip.getTenor().getPeriod().toTotalMonths() == nodePeriod.toTotalMonths()) {
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
    final HistoricalTimeSeriesResolutionResult resolutionResult = _htsResolver.resolve(ExternalIdBundle.of(externalId), null, null, null, "EXPOSURE", null);
    final ValueRequirement htsRequirement = HistoricalTimeSeriesFunctionUtils.createHTSRequirement(resolutionResult, "EXPOSURE", DateConstraint.VALUATION_TIME, true, DateConstraint.VALUATION_TIME,
        true);
    return htsRequirement;
    //return new ValueRequirement();
    //return new ValueRequirement(/*ExternalDataRequirementNames.SENSITIVITY*/"EXPOSURE", ComputationTargetType.PRIMITIVE, UniqueId.of(externalId.getScheme().getName(), externalId.getValue()));
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String curveCalculationConfig) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.with(ValuePropertyNames.CURVE, curveName);
    properties.with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetSpecification.of(currency), properties.get());
  }

  protected ValueRequirement getCurveSpecRequirement(final ComputationTarget target, final String curveName) {
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueProperties.Builder properties = ValueProperties.builder().with(ValuePropertyNames.CURVE, curveName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, ComputationTargetSpecification.of(currency), properties.get());
  }

}
