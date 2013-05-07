/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdsoption;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;
import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.NodalObjectsCurve;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CreditDefaultSwapOptionSecurityConverter;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.credit.CreditFunctionUtils;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToRecoveryRateVisitor;
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public abstract class ISDACreditDefaultSwapOptionFunction extends AbstractFunction.NonCompiledInvoker {
  private final String[] _valueRequirements;

  public ISDACreditDefaultSwapOptionFunction(final String... valueRequirements) {
    ArgumentChecker.notNull(valueRequirements, "value requirements");
    _valueRequirements = valueRequirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final OrganizationSource organizationSource = OpenGammaExecutionContext.getOrganizationSource(executionContext);
    final CreditDefaultSwapOptionSecurityConverter converter = new CreditDefaultSwapOptionSecurityConverter(securitySource, holidaySource, regionSource, organizationSource);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final CdsRecoveryRateIdentifier recoveryRateIdentifier = security.accept(new CreditSecurityToRecoveryRateVisitor(securitySource));
    final Object recoveryRateObject = inputs.getValue(new ValueRequirement("PX_LAST", ComputationTargetType.PRIMITIVE, recoveryRateIdentifier.getExternalId()));
    if (recoveryRateObject == null) {
      throw new OpenGammaRuntimeException("Could not get recovery rate");
    }
    final double recoveryRate = (Double) recoveryRateObject;
    CreditDefaultSwapOptionDefinition definition = security.accept(converter);
    definition = definition.withRecoveryRate(recoveryRate);
    final Object yieldCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (yieldCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve");
    }
    final Object spreadCurveObject = inputs.getValue(ValueRequirementNames.CREDIT_SPREAD_CURVE);
    if (spreadCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get credit spread curve");
    }
    final Object hazardRateCurveObject = inputs.getValue(ValueRequirementNames.HAZARD_RATE_CURVE);
    if (hazardRateCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get hazard rate curve");
    }
    final ISDADateCurve yieldCurve = (ISDADateCurve) yieldCurveObject;
    final double volatility = 0.3; //TODO
    final HazardRateCurve hazardRateCurve = (HazardRateCurve) hazardRateCurveObject;
    final NodalObjectsCurve<?, ?> spreadCurve = (NodalObjectsCurve<?, ?>) spreadCurveObject;
    final Tenor[] tenors = CreditFunctionUtils.getTenors(spreadCurve.getXData());
    final Double[] marketSpreadObjects = CreditFunctionUtils.getSpreads(spreadCurve.getYData());
    ParallelArrayBinarySort.parallelBinarySort(tenors, marketSpreadObjects);
    final int n = tenors.length;
    final List<ZonedDateTime> calibrationTimes = new ArrayList<>();
    final DoubleArrayList marketSpreads = new DoubleArrayList();
    for (int i = 0; i < n; i++) {
      final ZonedDateTime nextIMMDate = IMMDateGenerator.getNextIMMDate(valuationTime, tenors[i]).withHour(0).withMinute(0).withSecond(0).withNano(0);
      if (nextIMMDate.isAfter(definition.getOptionExerciseDate())) {
        calibrationTimes.add(IMMDateGenerator.getNextIMMDate(valuationTime, tenors[i]).withHour(0).withMinute(0).withSecond(0).withNano(0));
        marketSpreads.add(marketSpreadObjects[i]);
      }
    }
    if (calibrationTimes.size() < 2) {
      throw new OpenGammaRuntimeException("Need at least two credit spread points for pricing");
    }
    final ValueProperties properties = desiredValues.iterator().next().getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    return getComputedValue(definition, yieldCurve, volatility, calibrationTimes.toArray(new ZonedDateTime[calibrationTimes.size()]),
        marketSpreads.toDoubleArray(), hazardRateCurve, valuationTime, target, properties);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_OPTION_SECURITY;
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
    final Set<String> yieldCurveNames = constraints.getValues(PROPERTY_YIELD_CURVE);
    if (yieldCurveNames == null || yieldCurveNames.size() != 1) {
      return null;
    }
    final Set<String> yieldCurveCalculationConfigNames = constraints.getValues(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
    if (yieldCurveCalculationConfigNames == null || yieldCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> yieldCurveCalculationMethodNames = constraints.getValues(PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
    if (yieldCurveCalculationMethodNames == null || yieldCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    //    final Set<String> hazardRateCurveNames = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE);
    //    if (hazardRateCurveNames == null || hazardRateCurveNames.size() != 1) {
    //      return null;
    //    }
    final Set<String> hazardRateCurveCalculationMethodNames = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD);
    if (hazardRateCurveCalculationMethodNames == null || hazardRateCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    //    final Set<String> volatilitySurfaceNames = constraints.getValues(SURFACE);
    //    if (volatilitySurfaceNames == null || volatilitySurfaceNames.size() != 1) {
    //      return null;
    //    }
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final CreditSecurityToIdentifierVisitor identifierVisitor = new CreditSecurityToIdentifierVisitor(securitySource);
    final CreditDefaultSwapOptionSecurity security = (CreditDefaultSwapOptionSecurity) target.getSecurity();
    final String spreadCurveName = security.accept(identifierVisitor).getUniqueId().getValue();
    //TODO need to handle surface data as well
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(FinancialSecurityUtils.getCurrency(target.getSecurity()));
    final String yieldCurveName = Iterables.getOnlyElement(yieldCurveNames);
    final String yieldCurveCalculationConfigName = Iterables.getOnlyElement(yieldCurveCalculationConfigNames);
    final String yieldCurveCalculationMethodName = Iterables.getOnlyElement(yieldCurveCalculationMethodNames);
    final ValueRequirement yieldCurveRequirement = YieldCurveFunctionUtils.getCurveRequirement(currencyTarget, yieldCurveName, yieldCurveCalculationConfigName,
        yieldCurveCalculationMethodName);
    //final String hazardRateCurveName = Iterables.getOnlyElement(hazardRateCurveNames);
    final String hazardRateCurveCalculationMethod = Iterables.getOnlyElement(hazardRateCurveCalculationMethodNames);
    final Set<String> creditSpreadCurveShifts = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT);
    final Set<String> creditSpreadCurveShiftTypes = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT_TYPE);
    final ValueProperties.Builder hazardRateCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, spreadCurveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, hazardRateCurveCalculationMethod)
        .with(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG, yieldCurveCalculationConfigName)
        .with(PROPERTY_YIELD_CURVE_CALCULATION_METHOD, yieldCurveCalculationMethodName)
        .with(PROPERTY_YIELD_CURVE, yieldCurveName);
    final ValueProperties.Builder spreadCurveProperties = ValueProperties.builder()
        .with(CURVE, spreadCurveName);
    if (creditSpreadCurveShifts != null) {
      if (creditSpreadCurveShiftTypes == null || creditSpreadCurveShiftTypes.size() != 1) {
        return null;
      }
      hazardRateCurveProperties.with(PROPERTY_SPREAD_CURVE_SHIFT, creditSpreadCurveShifts).with(PROPERTY_SPREAD_CURVE_SHIFT_TYPE, creditSpreadCurveShiftTypes);
      spreadCurveProperties.with(PROPERTY_SPREAD_CURVE_SHIFT, creditSpreadCurveShifts).with(PROPERTY_SPREAD_CURVE_SHIFT_TYPE, creditSpreadCurveShiftTypes);
    }
    final ValueRequirement creditSpreadCurveRequirement = new ValueRequirement(ValueRequirementNames.CREDIT_SPREAD_CURVE, ComputationTargetSpecification.NULL, spreadCurveProperties.get());
    final ValueRequirement hazardRateCurveRequirement = new ValueRequirement(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), hazardRateCurveProperties.get());
    //    final String volatilitySurfaceName = Iterables.getOnlyElement(volatilitySurfaceNames);
    //    final ValueProperties volatilityProperties = ValueProperties.builder()
    //        .with(SURFACE, volatilitySurfaceName)
    //        .get();
    //    final ValueRequirement volSurfaceRequirement = new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ComputationTargetSpecification.NULL, volatilityProperties);
    final CdsRecoveryRateIdentifier recoveryRateIdentifier = security.accept(new CreditSecurityToRecoveryRateVisitor(securitySource));
    final ValueRequirement recoveryRateRequirement = new ValueRequirement("PX_LAST", ComputationTargetType.PRIMITIVE, recoveryRateIdentifier.getExternalId());
    return Sets.newHashSet(yieldCurveRequirement, creditSpreadCurveRequirement, hazardRateCurveRequirement, recoveryRateRequirement); //, volSurfaceRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder propertiesBuilder = getCommonResultProperties();
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification spec = entry.getKey();
      final ValueProperties.Builder inputPropertiesBuilder = spec.getProperties().copy();
      inputPropertiesBuilder.withoutAny(FUNCTION);
      final String valueName = spec.getValueName();
      if (valueName.equals(ValueRequirementNames.YIELD_CURVE)) {
        propertiesBuilder.with(PROPERTY_YIELD_CURVE, inputPropertiesBuilder.get().getValues(CURVE));
        inputPropertiesBuilder.withoutAny(CURVE);
        propertiesBuilder.with(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG, inputPropertiesBuilder.get().getValues(CURVE_CALCULATION_CONFIG));
        inputPropertiesBuilder.withoutAny(CURVE_CALCULATION_CONFIG);
        propertiesBuilder.with(PROPERTY_YIELD_CURVE_CALCULATION_METHOD, inputPropertiesBuilder.get().getValues(CURVE_CALCULATION_METHOD));
        inputPropertiesBuilder.withoutAny(CURVE_CALCULATION_METHOD);
      } else if (valueName.equals(ValueRequirementNames.CREDIT_SPREAD_CURVE)) {
        propertiesBuilder.with(PROPERTY_SPREAD_CURVE, inputPropertiesBuilder.get().getValues(CURVE));
        inputPropertiesBuilder.withoutAny(CURVE);
      } else if (valueName.equals(ValueRequirementNames.HAZARD_RATE_CURVE)) {
        propertiesBuilder.with(PROPERTY_HAZARD_RATE_CURVE, inputPropertiesBuilder.get().getValues(CURVE));
        inputPropertiesBuilder.withoutAny(CURVE);
        propertiesBuilder.with(PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD, inputPropertiesBuilder.get().getValues(CURVE_CALCULATION_METHOD));
        inputPropertiesBuilder.withoutAny(CURVE_CALCULATION_METHOD);
      }
      final ValueProperties inputProperties = inputPropertiesBuilder.get();
      if (!inputProperties.isEmpty()) {
        for (final String propertyName : inputProperties.getProperties()) {
          propertiesBuilder.with(propertyName, inputProperties.getValues(propertyName));
        }
      }
    }
    if (labelResultWithCurrency()) {
      propertiesBuilder.with(CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    }
    final ValueProperties properties = propertiesBuilder.get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirements) {
      results.add(new ValueSpecification(valueRequirement, targetSpec, properties));
    }
    return results;
  }

  protected abstract Set<ComputedValue> getComputedValue(CreditDefaultSwapOptionDefinition definition, ISDADateCurve yieldCurve, double vol, ZonedDateTime[] calibrationTenors,
      double[] marketSpreads, HazardRateCurve hazardRateCurve, ZonedDateTime valuationTime, ComputationTarget target, ValueProperties properties);

  protected abstract boolean labelResultWithCurrency();

  protected abstract ValueProperties.Builder getCommonResultProperties();
}
