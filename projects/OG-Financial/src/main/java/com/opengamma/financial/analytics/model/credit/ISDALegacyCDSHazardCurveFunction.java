/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_TOLERANCE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratemodel.CalibrateHazardRateCurve;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.CreditDefaultSwapSecurityConverter;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunctionHelper;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class ISDALegacyCDSHazardCurveFunction extends AbstractFunction {
  private CreditDefaultSwapSecurityConverter _converter;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    _converter = new CreditDefaultSwapSecurityConverter(holidaySource, regionSource);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @SuppressWarnings("synthetic-access")
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final Object yieldCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
        if (yieldCurveObject == null) {
          throw new OpenGammaRuntimeException("Could not get yield curve");
        }
        final Object yieldCurveSpecObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_SPEC);
        if (yieldCurveSpecObject == null) {
          throw new OpenGammaRuntimeException("Could not get yield curve specification");
        }
        final Object dataObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_MARKET_DATA);
        if (dataObject == null) {
          throw new OpenGammaRuntimeException("Could not get yield curve data");
        }
        final SnapshotDataBundle data = (SnapshotDataBundle) dataObject;
        final Map<ExternalId, Double> marketData = YieldCurveFunctionHelper.buildMarketDataMap(data);
        final InterpolatedYieldCurveSpecificationWithSecurities yieldCurveSpec = (InterpolatedYieldCurveSpecificationWithSecurities) yieldCurveSpecObject;
        final ISDACurve yieldCurve = (ISDACurve) yieldCurveObject;
        final ZonedDateTime now = executionContext.getValuationClock().zonedDateTime();
        final LegacyVanillaCDSSecurity security = (LegacyVanillaCDSSecurity) target.getSecurity();
        final LegacyCreditDefaultSwapDefinition cds = _converter.visitLegacyVanillaCDSSecurity(security);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final String curveName = desiredValue.getConstraint(ValuePropertyNames.CURVE);
        final String nIterationsName = desiredValue.getConstraint(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS);
        final String toleranceName = desiredValue.getConstraint(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE);
        final String rangeMultiplierName = desiredValue.getConstraint(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER);
        final int maxIterations = Integer.parseInt(nIterationsName);
        final double tolerance = Double.parseDouble(toleranceName);
        final double rangeMultiplier = Double.parseDouble(rangeMultiplierName);
        final CalibrateHazardRateCurve calibrationCalculator = new CalibrateHazardRateCurve(maxIterations, tolerance, rangeMultiplier);
        final Set<FixedIncomeStripWithSecurity> strips = yieldCurveSpec.getStrips();
        final int nStrips = strips.size();
        final ZonedDateTime[] tenors = new ZonedDateTime[nStrips];
        final double[] marketSpreads = new double[nStrips];
        int i = 0;
        for (final FixedIncomeStripWithSecurity strip : strips) {
          if (strip.getInstrumentType() != StripInstrumentType.SIMPLE_ZERO_DEPOSIT) {
            throw new OpenGammaRuntimeException("Strips for hazard curve must be of type " + StripInstrumentType.SIMPLE_ZERO_DEPOSIT);
          }
          tenors[i] = strip.getMaturity();
          marketSpreads[i++] = marketData.get(strip.getSecurityIdentifier());
        }
        final HazardRateCurve curve = calibrationCalculator.getCalibratedHazardRateCurve(now, cds, tenors, marketSpreads, yieldCurve, PriceType.CLEAN);
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE, curveName)
            .with(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS, nIterationsName)
            .with(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE, toleranceName)
            .with(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER, rangeMultiplierName).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, curve));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.SECURITY;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.SECURITY) {
          return false;
        }
        return target.getSecurity() instanceof LegacyVanillaCDSSecurity;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .withAny(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS)
            .withAny(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE)
            .withAny(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER).get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), properties));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final Set<String> yieldCurveNames = constraints.getValues(PROPERTY_YIELD_CURVE);
        if (yieldCurveNames == null || yieldCurveNames.size() != 1) {
          return null;
        }
        final Set<String> yieldCurveCalculationConfigs = constraints.getValues(PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
        if (yieldCurveCalculationConfigs == null || yieldCurveCalculationConfigs.size() != 1) {
          return null;
        }
        final Set<String> yieldCurveCalculationMethods = constraints.getValues(PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
        if (yieldCurveCalculationMethods == null || yieldCurveCalculationMethods.size() != 1) {
          return null;
        }
        final Set<String> nIterationsName = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS);
        if (nIterationsName == null || nIterationsName.size() != 1) {
          return null;
        }
        final Set<String> tolerances = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE);
        if (tolerances == null || tolerances.size() != 1) {
          return null;
        }
        final Set<String> rangeMultipliers = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER);
        if (rangeMultipliers == null || rangeMultipliers.size() != 1) {
          return null;
        }
        final String curveName = Iterables.getOnlyElement(curveNames);
        final String yieldCurveName = Iterables.getOnlyElement(yieldCurveNames);
        final String yieldCurveCalculationConfig = Iterables.getOnlyElement(yieldCurveCalculationConfigs);
        final String yieldCurveCalculationMethod = Iterables.getOnlyElement(yieldCurveCalculationMethods);
        final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
        final ValueProperties curveDefinitionProperties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, curveName).get();
        final ValueProperties yieldCurveProperties = ValueProperties.builder()
            .with(ValuePropertyNames.CURVE, yieldCurveName)
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, yieldCurveCalculationConfig)
            .with(ValuePropertyNames.CURVE_CALCULATION_METHOD, yieldCurveCalculationMethod).get();
        final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(3);
        final ComputationTargetSpecification targetSpec = target.toSpecification();
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, targetSpec, curveDefinitionProperties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE_SPEC, targetSpec, curveDefinitionProperties));
        requirements.add(new ValueRequirement(ValueRequirementNames.YIELD_CURVE, currency, yieldCurveProperties));
        return requirements;
      }

    };
  }

}
