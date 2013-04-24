/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.calibration;

import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndSpreadsProvider;
import com.opengamma.analytics.financial.credit.calibratehazardratecurve.ISDAHazardRateCurveCalculator;
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
import com.opengamma.financial.analytics.model.cds.ISDAFunctionConstants;
import com.opengamma.financial.analytics.model.credit.CreditFunctionUtils;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class ISDACDSOptionHazardRateCurveFunction extends AbstractFunction.NonCompiledInvoker {
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final ISDAHazardRateCurveCalculator CALCULATOR = new ISDAHazardRateCurveCalculator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final OrganizationSource organizationSource = OpenGammaExecutionContext.getOrganizationSource(executionContext);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final CreditDefaultSwapOptionSecurityConverter converter = new CreditDefaultSwapOptionSecurityConverter(securitySource, holidaySource, regionSource, organizationSource);
    final CreditDefaultSwapOptionSecurity security = (CreditDefaultSwapOptionSecurity) target.getSecurity();
    final CreditDefaultSwapOptionDefinition cdsOption = security.accept(converter);
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
    final Tenor[] tenors = CreditFunctionUtils.getTenors(spreadCurve.getXData());
    final Double[] marketSpreadObjects = CreditFunctionUtils.getSpreads(spreadCurve.getYData());
    ParallelArrayBinarySort.parallelBinarySort(tenors, marketSpreadObjects);
    final int n = tenors.length;
    final List<ZonedDateTime> calibrationTimes = new ArrayList<>();
    final DoubleArrayList marketSpreads = new DoubleArrayList();
    for (int i = 0; i < n; i++) {
      final ZonedDateTime nextIMMDate = IMMDateGenerator.getNextIMMDate(valuationTime, tenors[i]).withHour(0).withMinute(0).withSecond(0).withNano(0);
      if (nextIMMDate.isAfter(cdsOption.getOptionExerciseDate())) {
        calibrationTimes.add(IMMDateGenerator.getNextIMMDate(valuationTime, tenors[i]).withHour(0).withMinute(0).withSecond(0).withNano(0));
        marketSpreads.add(marketSpreadObjects[i]);
      }
    }
    if (calibrationTimes.size() < 2) {
      throw new OpenGammaRuntimeException("Need at least two credit spread points for pricing");
    }
    final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    final ISDAYieldCurveAndSpreadsProvider data = new ISDAYieldCurveAndSpreadsProvider(calibrationTimes.toArray(new ZonedDateTime[calibrationTimes.size()]),
        marketSpreads.toDoubleArray(), yieldCurve);
    final HazardRateCurve curve = CALCULATOR.calibrateHazardRateCurve(cdsOption.getUnderlyingCDS(), data, valuationTime);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, curve));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_OPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), properties));
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
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(FinancialSecurityUtils.getCurrency(target.getSecurity()));
    final String yieldCurveName = Iterables.getOnlyElement(yieldCurveNames);
    final String yieldCurveCalculationConfigName = Iterables.getOnlyElement(yieldCurveCalculationConfigNames);
    final String yieldCurveCalculationMethodName = Iterables.getOnlyElement(yieldCurveCalculationMethodNames);
    final CreditSecurityToIdentifierVisitor identifierVisitor = new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(context));
    final String spreadCurveName = security.accept(identifierVisitor).getUniqueId().getValue();
    final ValueRequirement yieldCurveRequirement = YieldCurveFunctionUtils.getCurveRequirement(currencyTarget, yieldCurveName, yieldCurveCalculationConfigName,
        yieldCurveCalculationMethodName);

    final ValueProperties.Builder spreadCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, spreadCurveName);
    final Set<String> spreadCurveShifts = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT);
    if (spreadCurveShifts != null && !spreadCurveShifts.isEmpty()) {
      final Set<String> creditSpreadCurveShiftTypes = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT_TYPE);
      if (creditSpreadCurveShiftTypes == null || creditSpreadCurveShiftTypes.size() != 1) {
        return null;
      }
      spreadCurveProperties.with(PROPERTY_SPREAD_CURVE_SHIFT, spreadCurveShifts).with(PROPERTY_SPREAD_CURVE_SHIFT_TYPE, creditSpreadCurveShiftTypes);
    }
    final ValueRequirement creditSpreadCurveRequirement = new ValueRequirement(ValueRequirementNames.CREDIT_SPREAD_CURVE, ComputationTargetSpecification.NULL, spreadCurveProperties.get());
    return Sets.newHashSet(yieldCurveRequirement, creditSpreadCurveRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder propertiesBuilder = createValueProperties()
        .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, ISDAFunctionConstants.ISDA_METHOD_NAME);
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification spec = entry.getKey();
      final ValueProperties.Builder inputPropertiesBuilder = spec.getProperties().copy();
      inputPropertiesBuilder.withoutAny(ValuePropertyNames.FUNCTION);
      if (spec.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE);
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE_CALCULATION_METHOD);
      } else if (spec.getValueName().equals(ValueRequirementNames.CREDIT_SPREAD_CURVE)) {
        propertiesBuilder.with(ValuePropertyNames.CURVE, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE);
      }
      final ValueProperties inputProperties = inputPropertiesBuilder.get();
      if (!inputProperties.isEmpty()) {
        for (final String propertyName : inputProperties.getProperties()) {
          propertiesBuilder.with(propertyName, inputProperties.getValues(propertyName));
        }
      }
    }
    final ValueProperties properties = propertiesBuilder.get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE, targetSpec, properties));
  }

}
