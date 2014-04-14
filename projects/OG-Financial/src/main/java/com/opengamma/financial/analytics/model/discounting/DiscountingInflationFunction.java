/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.InflationSwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;

/**
 * Base function for all inflation swap pricing and risk functions that use
 * curves constructed using the discounting method.
 */
public abstract class DiscountingInflationFunction extends DiscountingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public DiscountingInflationFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  protected DefaultTradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final InflationSwapSecurityConverter swapConverter = new InflationSwapSecurityConverter(securitySource, conventionSource, regionSource, holidaySource);
    final FutureTradeConverter futureTradeConverter = new FutureTradeConverter();
    return new DefaultTradeConverter(futureTradeConverter, swapConverter);
  }

  /**
   * Base compiled function for all pricing and risk functions that use a curves constructed
   * using the discounting method.
   */
  protected abstract class DiscountingInflationCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property
     */
    protected DiscountingInflationCompiledFunction(final DefaultTradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter, final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getTrade().getSecurity();
      return security instanceof ZeroCouponInflationSwapSecurity ||
          security instanceof YearOnYearInflationSwapSecurity;
    }

  }
}
