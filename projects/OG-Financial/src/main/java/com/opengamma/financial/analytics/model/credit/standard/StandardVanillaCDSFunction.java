/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.standard;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.NodalObjectsCurve;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.analytics.conversion.CreditDefaultSwapSecurityConverter;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public abstract class StandardVanillaCDSFunction extends AbstractFunction.NonCompiledInvoker {
  private final String[] _valueRequirements;
  private CreditDefaultSwapSecurityConverter _converter;

  public StandardVanillaCDSFunction(final String... valueRequirements) {
    ArgumentChecker.notNull(valueRequirements, "value requirements");
    _valueRequirements = valueRequirements;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final OrganizationSource organizationSource = null; //OpenGammaCompilationContext.getOrganizationSource(context);
    _converter = new CreditDefaultSwapSecurityConverter(holidaySource, regionSource, organizationSource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    final CreditDefaultSwapSecurity security = (CreditDefaultSwapSecurity) target.getSecurity();
    final LegacyVanillaCreditDefaultSwapDefinition definition;
    if (security instanceof StandardVanillaCDSSecurity) {
      definition = _converter.visitStandardVanillaCDSSecurity((StandardVanillaCDSSecurity) security, valuationTime);
    } else {
      definition = _converter.visitLegacyVanillaCDSSecurity((LegacyVanillaCDSSecurity) security, valuationTime);
    }
    final Object yieldCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (yieldCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve");
    }
    final Object spreadCurveObject = inputs.getValue(ValueRequirementNames.CREDIT_SPREAD_CURVE);
    if (spreadCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get credit spread curve");
    }
    final ISDADateCurve yieldCurve = (ISDADateCurve) yieldCurveObject;
    final NodalObjectsCurve<?, ?> spreadCurve = (NodalObjectsCurve<?, ?>) spreadCurveObject;
    final Tenor[] tenors = (Tenor[]) spreadCurve.getXData();
    final Double[] marketSpreadObjects = (Double[]) spreadCurve.getYData();
    final int n = tenors.length;
    final ZonedDateTime[] times = new ZonedDateTime[n];
    final double[] marketSpreads = new double[n];
    for (int i = 0; i < n; i++) {
      times[i] = IMMDateGenerator.getNextIMMDate(valuationTime, tenors[i]);
      marketSpreads[i] = marketSpreadObjects[i];
    }
    final ValueProperties properties = desiredValues.iterator().next().getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    return getComputedValue(definition, yieldCurve, times, marketSpreads, valuationTime, target, properties);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.STANDARD_VANILLA_CDS_SECURITY.or(FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirements) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> yieldCurveNames = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE);
    if (yieldCurveNames == null || yieldCurveNames.size() != 1) {
      return null;
    }
    final Set<String> yieldCurveCalculationConfigNames = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
    if (yieldCurveCalculationConfigNames == null || yieldCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> yieldCurveCalculationMethodNames = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
    if (yieldCurveCalculationMethodNames == null || yieldCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final String spreadCurveName = security.accept(CreditSecurityToIdentifierVisitor.getInstance()).getUniqueId().getValue();
    //    final Set<String> spreadCurveNames = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE);
    //    if (spreadCurveNames == null || spreadCurveNames.size() != 1) {
    //      return null;
    //    }
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(FinancialSecurityUtils.getCurrency(target.getSecurity()));
    final String yieldCurveName = Iterables.getOnlyElement(yieldCurveNames);
    final String yieldCurveCalculationConfigName = Iterables.getOnlyElement(yieldCurveCalculationConfigNames);
    final String yieldCurveCalculationMethodName = Iterables.getOnlyElement(yieldCurveCalculationMethodNames);
    //    final String spreadCurveName = Iterables.getOnlyElement(spreadCurveNames);
    final ValueRequirement yieldCurveRequirement = YieldCurveFunctionUtils.getCurveRequirement(currencyTarget, yieldCurveName, yieldCurveCalculationConfigName,
        yieldCurveCalculationMethodName);
    final ValueProperties spreadCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, spreadCurveName)
        .get();
    final ValueRequirement creditSpreadCurveRequirement = new ValueRequirement(ValueRequirementNames.CREDIT_SPREAD_CURVE, ComputationTargetSpecification.NULL, spreadCurveProperties);
    return Sets.newHashSet(yieldCurveRequirement, creditSpreadCurveRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder propertiesBuilder = createValueProperties();
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification spec = entry.getKey();
      ValueProperties inputProperties = spec.getProperties().copy().get();
      inputProperties = inputProperties.withoutAny(ValuePropertyNames.FUNCTION);
      if (spec.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE, inputProperties.getValues(ValuePropertyNames.CURVE));
        inputProperties = inputProperties.withoutAny(ValuePropertyNames.CURVE);
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG, inputProperties.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        inputProperties = inputProperties.withoutAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD, inputProperties.getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD));
        inputProperties = inputProperties.withoutAny(ValuePropertyNames.CURVE_CALCULATION_METHOD);
      } else if (spec.getValueName().equals(ValueRequirementNames.CREDIT_SPREAD_CURVE)) {
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE, inputProperties.getValues(ValuePropertyNames.CURVE));
        inputProperties = inputProperties.withoutAny(ValuePropertyNames.CURVE);
      }
      if (!inputProperties.isEmpty()) {
        for (final String propertyName : inputProperties.getProperties()) {
          propertiesBuilder.with(propertyName, inputProperties.getValues(propertyName));
        }
      }
    }
    propertiesBuilder.withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_CDS_PRICE_TYPE)
    .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_BUMP)
    .withAny(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_BUMP_TYPE);
    final ValueProperties properties = propertiesBuilder.get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirements) {
      results.add(new ValueSpecification(valueRequirement, targetSpec, properties));
    }
    return results;
  }

  protected abstract Set<ComputedValue> getComputedValue(LegacyVanillaCreditDefaultSwapDefinition definition, ISDADateCurve yieldCurve, ZonedDateTime[] times, double[] marketSpreads,
      ZonedDateTime valuationTime, ComputationTarget target, ValueProperties properties);
}
