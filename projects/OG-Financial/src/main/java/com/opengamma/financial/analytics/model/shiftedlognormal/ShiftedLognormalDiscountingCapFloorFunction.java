/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.shiftedlognormal;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.LOGNORMAL_SURFACE_SHIFTS;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.CAP_FLOOR;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.SHIFTED_LOGNORMAL;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.IBOR;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.getConventionName;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.option.parameters.BlackSmileShiftCapParameters;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSmileShiftCapProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSmileShiftCapProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CapFloorSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * Base function for all cap/floor pricing and risk functions that use a Black surface and curves constructed using the discounting method.
 */
public abstract class ShiftedLognormalDiscountingCapFloorFunction extends DiscountingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public ShiftedLognormalDiscountingCapFloorFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  protected TradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final CapFloorSecurityConverter converter = new CapFloorSecurityConverter(holidaySource, conventionSource, regionSource);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().capFloorVisitor(converter).create();
    final FutureTradeConverter futureTradeConverter = new FutureTradeConverter(securitySource, holidaySource, conventionSource, conventionBundleSource, regionSource);
    return new TradeConverter(futureTradeConverter, securityConverter);
  }

  /**
   * Base compiled function for all pricing and risk functions that use a shifted lognormal surface and curves constructed using the discounting method.
   */
  protected abstract class ShiftedLognormalDiscountingCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property.
     */
    protected ShiftedLognormalDiscountingCompiledFunction(final TradeConverter tradeToDefinitionConverter, final FixedIncomeConverterDataProvider definitionToDerivativeConverter,
        final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getTrade().getSecurity();
      return security instanceof CapFloorSecurity;
    }

    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties().with(PROPERTY_CURVE_TYPE, DISCOUNTING).with(PROPERTY_VOLATILITY_MODEL, SHIFTED_LOGNORMAL)
          .with(LognormalVolatilityShiftFunction.SHIFT_CURVE, LognormalVolatilityShiftFunction.TEST).withAny(SURFACE).withAny(CURVE_EXPOSURES);
      if (isWithCurrency()) {
        final Security security = target.getTrade().getSecurity();
        final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
        properties.with(CURRENCY, currency);
        return Collections.singleton(properties);
      }
      return Collections.singleton(properties);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      final ValueProperties constraints = desiredValue.getConstraints();
      final Currency currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity());
      final Set<String> surface = constraints.getValues(SURFACE);
      final ValueProperties properties = ValueProperties.builder().with(SURFACE, surface).with(PROPERTY_SURFACE_INSTRUMENT_TYPE, CAP_FLOOR).get();
      final ValueRequirement surfaceRequirement = new ValueRequirement(INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
      requirements.add(surfaceRequirement);
      final Set<String> shiftCurve = constraints.getValues(LognormalVolatilityShiftFunction.SHIFT_CURVE);
      final ValueProperties shiftProperties = ValueProperties.builder().with(LognormalVolatilityShiftFunction.SHIFT_CURVE, shiftCurve).get();
      final ValueRequirement lognormalShiftRequirement = new ValueRequirement(LOGNORMAL_SURFACE_SHIFTS, ComputationTargetSpecification.NULL, shiftProperties);
      requirements.add(lognormalShiftRequirement);
      return requirements;
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> surfaceNames = constraints.getValues(SURFACE);
      if (surfaceNames == null) {
        return false;
      }
      return super.requirementsSet(constraints);
    }

    /**
     * Gets the Black surface and curve data.
     * 
     * @param executionContext The execution context, not null
     * @param inputs The function inputs, not null
     * @param target The computation target, not null
     * @param fxMatrix The FX matrix, not null
     * @return The Black surface and curve data
     */
    protected BlackSmileShiftCapProviderInterface getBlackSurface(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final FXMatrix fxMatrix) {
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
      final CapFloorSecurity security = (CapFloorSecurity) target.getTrade().getSecurity();
      final Currency currency = FinancialSecurityUtils.getCurrency(security);
      final String iborConventionName = getConventionName(currency, IBOR);
      final IborIndexConvention iborIndexConvention = conventionSource.getSingle(ExternalId.of(SCHEME_NAME, iborConventionName), IborIndexConvention.class);
      final Frequency freqIbor = security.getFrequency();
      final Period tenorIbor = getTenor(freqIbor);
      final int spotLag = iborIndexConvention.getSettlementDays();
      final IborIndex iborIndex = new IborIndex(currency, tenorIbor, spotLag, iborIndexConvention.getDayCount(), iborIndexConvention.getBusinessDayConvention(),
          iborIndexConvention.isIsEOM(), iborIndexConvention.getName());
      final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
      final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(INTERPOLATED_VOLATILITY_SURFACE);
      final DoublesCurve shiftCurve = (DoublesCurve) inputs.getValue(LOGNORMAL_SURFACE_SHIFTS);
      final BlackSmileShiftCapParameters parameters = new BlackSmileShiftCapParameters(volatilitySurface.getSurface(), shiftCurve, iborIndex);
      final BlackSmileShiftCapProviderInterface blackData = new BlackSmileShiftCapProvider(data, parameters);
      return blackData;
    }

    /**
     * Gets a tenor from a frequency.
     * 
     * @param freq The frequency
     * @return The tenor, not null
     */
    private Period getTenor(final Frequency freq) {
      if (freq instanceof PeriodFrequency) {
        return ((PeriodFrequency) freq).getPeriod();
      } else if (freq instanceof SimpleFrequency) {
        return ((SimpleFrequency) freq).toPeriodFrequency().getPeriod();
      }
      throw new OpenGammaRuntimeException("Can only PeriodFrequency or SimpleFrequency; have " + freq.getClass());
    }

  }
}
