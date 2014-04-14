/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.engine.value.ValueRequirementNames.INTERPOLATED_VOLATILITY_SURFACE;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.IR_FUTURE_OPTION;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.BLACK;
import static com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues.PROPERTY_VOLATILITY_MODEL;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.EURODOLLAR_FUTURE;
import static com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper.SCHEME_NAME;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Period;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctionHelper;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * Base function for all interest rate future option pricing and risk functions that use a Black surface and curves constructed using the discounting method.
 */
public abstract class BlackDiscountingIRFutureOptionFunction extends DiscountingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public BlackDiscountingIRFutureOptionFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  protected DefaultTradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final InterestRateFutureOptionSecurityConverter irFutureOptionConverter = new InterestRateFutureOptionSecurityConverter(holidaySource, conventionSource, regionSource, securitySource);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .irfutureOptionVisitor(irFutureOptionConverter).create();
    final FutureTradeConverter futureTradeConverter = new FutureTradeConverter();
    return new DefaultTradeConverter(futureTradeConverter, securityConverter);
  }

  /**
   * Base compiled function for all pricing and risk functions that use a Black surface and curves constructed using the discounting method.
   */
  protected abstract class BlackDiscountingCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property.
     */
    protected BlackDiscountingCompiledFunction(final DefaultTradeConverter tradeToDefinitionConverter, final FixedIncomeConverterDataProvider definitionToDerivativeConverter,
        final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getTrade().getSecurity();
      return security instanceof IRFutureOptionSecurity;
    }

    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties().with(PROPERTY_CURVE_TYPE, DISCOUNTING).with(PROPERTY_VOLATILITY_MODEL, BLACK).withAny(SURFACE)
          .withAny(CURVE_EXPOSURES);
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
      final Set<String> surfaces = constraints.getValues(SURFACE);
      //TODO there should be a different target used here so it's not necessary to do the append
      final String surface = Iterables.getOnlyElement(surfaces) + "_" + IRFutureOptionFunctionHelper.getFutureOptionPrefix(target);
      final ValueProperties properties = ValueProperties.builder().with(SURFACE, surface).with(PROPERTY_SURFACE_INSTRUMENT_TYPE, IR_FUTURE_OPTION).get();
      final ValueRequirement surfaceRequirement = new ValueRequirement(INTERPOLATED_VOLATILITY_SURFACE, ComputationTargetSpecification.of(currency), properties);
      requirements.add(surfaceRequirement);
      return requirements;
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> surfaceNames = constraints.getValues(SURFACE);
      if (surfaceNames == null || surfaceNames.size() != 1) {
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
    protected BlackSTIRFuturesProviderInterface getBlackSurface(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final FXMatrix fxMatrix) {
      final ConventionSource conventionSource = OpenGammaExecutionContext.getConventionSource(executionContext);
      final IRFutureOptionSecurity security = (IRFutureOptionSecurity) target.getTrade().getSecurity();
      final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
      final VolatilitySurface volatilitySurface = (VolatilitySurface) inputs.getValue(INTERPOLATED_VOLATILITY_SURFACE);
      final Currency currency = security.getCurrency();
      // TODO the convention name should not be hard-coded, but there's no way of getting this information until
      // there's a convention link in the security.
      final InterestRateFutureConvention convention = conventionSource.getSingle(ExternalId.of(SCHEME_NAME, EURODOLLAR_FUTURE), InterestRateFutureConvention.class);
      final IborIndexConvention iborIndexConvention = conventionSource.getSingle(convention.getIndexConvention(), IborIndexConvention.class);
      final Period period = Period.ofMonths(3); //TODO
      final int spotLag = iborIndexConvention.getSettlementDays();
      final IborIndex iborIndex = new IborIndex(currency, period, spotLag, iborIndexConvention.getDayCount(), iborIndexConvention.getBusinessDayConvention(), iborIndexConvention.isIsEOM(),
          iborIndexConvention.getName());
      final BlackSTIRFuturesSmileProvider blackData = new BlackSTIRFuturesSmileProvider(data, volatilitySurface.getSurface(), iborIndex);
      return blackData;
    }

  }
}
