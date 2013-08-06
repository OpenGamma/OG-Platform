/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.multicurve.hullwhite;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.HULL_WHITE_DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_PARAMETERS;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.analytics.model.multicurve.InstrumentTypeHelper;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * Base function for all pricing and risk functions that use the discounting curve
 * construction method.
 */
public abstract class HullWhiteFunction extends MultiCurvePricingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public HullWhiteFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  /**
   * Constructs an object capable of converting from {@link ComputationTarget} to {@link InstrumentDefinition}.
   * @param context The compilation context, not null
   * @return The converter
   */
  protected FinancialSecurityVisitor<InstrumentDefinition<?>> getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    return FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .cashSecurityVisitor(cashConverter)
        .fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter).create();
  }

  /**
   * Constructs and object capable of converting from {@link InstrumentDefinition} to {@link InstrumentDerivative}.
   * @param context The compilation context, not null
   * @return The converter
   */
  protected FixedIncomeConverterDataProvider getDefinitionToDerivativeConverter(final FunctionCompilationContext context) {
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    return new FixedIncomeConverterDataProvider(conventionBundleSource, timeSeriesResolver);
  }

  /**
   * Base compiled function for all pricing and risk functions that use the Hull-White one-factor
   * curve construction method.
   */
  protected abstract class HullWhiteCompiledFunction extends MultiCurveCompiledFunction {
    /** Converts targets to definitions */
    private final FinancialSecurityVisitor<InstrumentDefinition<?>> _targetToDefinitionConverter;
    /** Converts definitions to derivatives */
    private final FixedIncomeConverterDataProvider _definitionToDerivativeConverter;

    /**
     * @param targetToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     */
    protected HullWhiteCompiledFunction(final FinancialSecurityVisitor<InstrumentDefinition<?>> targetToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter) {
      ArgumentChecker.notNull(targetToDefinitionConverter, "target to definition converter");
      ArgumentChecker.notNull(definitionToDerivativeConverter, "definition to derivative converter");
      _targetToDefinitionConverter = targetToDefinitionConverter;
      _definitionToDerivativeConverter = definitionToDerivativeConverter;
    }

    @Override
    public ComputationTargetType getTargetType() {
      return InstrumentTypeHelper.LINEAR_FIXED_INCOME_INSTRUMENT_TYPE;
    }

    @Override
    protected FinancialSecurity getSecurityFromTarget(final ComputationTarget target) {
      return (FinancialSecurity) target.getSecurity();
    }

    @Override
    protected InstrumentDefinition<?> getDefinitionFromTarget(final ComputationTarget target) {
      return getSecurityFromTarget(target).accept(_targetToDefinitionConverter);
    }

    @Override
    protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
      return createValueProperties()
          .with(PROPERTY_CURVE_TYPE, HULL_WHITE_DISCOUNTING)
          .withAny(CURVE_EXPOSURES)
          .withAny(PROPERTY_HULL_WHITE_PARAMETERS)
          .with(CURRENCY, FinancialSecurityUtils.getCurrency(getSecurityFromTarget(target)).getCode()); //TODO really shouldn't be using CURRENCY for this
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> curveExposureConfigs = constraints.getValues(CURVE_EXPOSURES);
      if (curveExposureConfigs == null) {
        return false;
      }
      final Set<String> hullWhiteParameters = constraints.getValues(PROPERTY_HULL_WHITE_PARAMETERS);
      if (hullWhiteParameters == null || hullWhiteParameters.size() != 1) {
        return false;
      }
      return true;
    }

    @Override
    protected Builder getCurveProperties(final ComputationTarget target, final ValueProperties constraints) {
      final String currency = FinancialSecurityUtils.getCurrency(getSecurityFromTarget(target)).getCode();
      final Set<String> hullWhiteParameters = constraints.getValues(PROPERTY_HULL_WHITE_PARAMETERS);
      return ValueProperties.builder()
          .with(PROPERTY_HULL_WHITE_PARAMETERS, hullWhiteParameters)
          .with(CURRENCY, currency);
    }

    @Override
    protected Set<ValueRequirement> getConversionTimeSeriesRequirements(final FunctionCompilationContext context, final ComputationTarget target,
        final InstrumentDefinition<?> definition) {
      return _definitionToDerivativeConverter.getConversionTimeSeriesRequirements(getSecurityFromTarget(target), definition);
    }

    @Override
    protected InstrumentDerivative getDerivative(final ComputationTarget target, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries, final InstrumentDefinition<?> definition) {
      return _definitionToDerivativeConverter.convert(getSecurityFromTarget(target), definition, now, timeSeries);
    }
  }
}
