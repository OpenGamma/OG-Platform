/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondTotalReturnSwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;

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
  protected TradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = new BondTotalReturnSwapSecurityConverter(conventionSource,
        holidaySource, regionSource, securitySource);
    return new TradeConverter(securityConverter);
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
    protected BondTotalReturnSwapCompiledFunction(final TradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter, final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return target.getTrade().getSecurity() instanceof BondTotalReturnSwapSecurity;
    }

  }
}
