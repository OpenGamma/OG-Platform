/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondTotalReturnSwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Base function for bond total return swap pricing.
 */
public abstract class BondTotalReturnSwapFunction extends DiscountingFunction {

  /**
   * @param valueRequirements The value requirement names, not null
   */
  public BondTotalReturnSwapFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  protected DefaultTradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = new BondTotalReturnSwapSecurityConverter(conventionSource,
        holidaySource, regionSource, securitySource);
    return new DefaultTradeConverter(securityConverter);
  }

  /**
   * Base compiled function for bond total return swap pricing.
   */
  protected abstract class BondTotalReturnSwapCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the {@link ValuePropertyNames#CURRENCY} result property is set
     */
    protected BondTotalReturnSwapCompiledFunction(final DefaultTradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter, final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return target.getTrade().getSecurity() instanceof BondTotalReturnSwapSecurity;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties()
          .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
          .withAny(CURVE_EXPOSURES);
      if (isWithCurrency()) {
        properties.with(CURRENCY, getCurrencyOfResult((BondTotalReturnSwapSecurity) target.getTrade().getSecurity()));
      }
      return Collections.singleton(properties);
    }

    @Override
    protected MulticurveProviderDiscount getMergedProviders(final FunctionInputs inputs, final FXMatrix matrix) {
      throw new IllegalStateException("Should not use this method");
    }

    /**
     * Merges the multi-curve providers and issuer providers.
     *
     * @param inputs The function inputs, not null
     * @param matrix The FX matrix, not null
     * @return The merged providers
     */
    protected IssuerProviderInterface getMergedWithIssuerProviders(final FunctionInputs inputs, final FXMatrix matrix) {
      ArgumentChecker.notNull(inputs, "inputs");
      ArgumentChecker.notNull(matrix, "matrix");
      final Collection<MulticurveProviderDiscount> providers = new HashSet<>();
      IssuerProvider issuerCurves = null;
      for (final ComputedValue input : inputs.getAllValues()) {
        final String valueName = input.getSpecification().getValueName();
        if (CURVE_BUNDLE.equals(valueName)) {
          final ParameterProviderInterface generic = (ParameterProviderInterface) input.getValue();
          if (generic instanceof MulticurveProviderInterface) {
            providers.add((MulticurveProviderDiscount) generic.getMulticurveProvider());
          } else if (generic instanceof IssuerProvider) {
            issuerCurves = (IssuerProvider) generic;
          }
        }
      }
      if (issuerCurves == null) {
        throw new OpenGammaRuntimeException("Could not get issuer curves");
      }
      final MulticurveProviderDiscount result = ProviderUtils.mergeDiscountingProviders(providers);
      final MulticurveProviderDiscount merged = ProviderUtils.mergeDiscountingProviders(result, matrix);
      return new IssuerProviderDiscount(merged, issuerCurves.getIssuerCurves());
    }

    /**
     * Gets the currency of the result.
     * @param security The bond TRS.
     * @return The result currency
     */
    protected abstract String getCurrencyOfResult(BondTotalReturnSwapSecurity security);
  }
}
