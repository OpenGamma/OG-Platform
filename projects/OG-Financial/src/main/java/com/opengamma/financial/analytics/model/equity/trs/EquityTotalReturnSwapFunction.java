/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.trs;

import static com.opengamma.core.value.MarketDataRequirementNames.MARKET_VALUE;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.EquityTotalReturnSwapSecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.analytics.model.discounting.DiscountingFunction;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.id.ExternalIdBundle;

/**
 * Base function for equity total return swap pricing.
 */
public abstract class EquityTotalReturnSwapFunction extends DiscountingFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(EquityTotalReturnSwapFunction.class);

  /**
   * @param valueRequirements The value requirement names, not null
   */
  public EquityTotalReturnSwapFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  @Override
  protected TradeConverter getTargetToDefinitionConverter(final FunctionCompilationContext context) {
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final FinancialSecurityVisitor<InstrumentDefinition<?>> securityConverter = new EquityTotalReturnSwapSecurityConverter(conventionSource,
        holidaySource, securitySource);
    return new TradeConverter(securityConverter);
  }

  /**
   * Base compiled function for equity total return swap pricing.
   */
  protected abstract class EquityTotalReturnSwapCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the {@link ValuePropertyNames#CURRENCY} result property is set
     */
    protected EquityTotalReturnSwapCompiledFunction(final TradeConverter tradeToDefinitionConverter,
        final FixedIncomeConverterDataProvider definitionToDerivativeConverter, final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      return target.getTrade().getSecurity() instanceof EquityTotalReturnSwapSecurity;
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      final ValueRequirement spotRequirement = getEquityUnderlyingRequirement(context, target);
      if (spotRequirement == null) {
        return null;
      }
      requirements.add(spotRequirement);
      return requirements;
    }

    /**
     * Gets the underlying equity market data requirement.
     * @param context The compilation context
     * @param target The computation target
     * @return The equity market data requirement
     */
    @SuppressWarnings("synthetic-access")
    protected ValueRequirement getEquityUnderlyingRequirement(final FunctionCompilationContext context, final ComputationTarget target) {
      final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
      final EquityTotalReturnSwapSecurity security = (EquityTotalReturnSwapSecurity) target.getTrade().getSecurity();
      final ExternalIdBundle equityId = security.getAssetId();
      try {
        final Security equitySecurity = securitySource.getSingle(equityId);
        return new ValueRequirement(MARKET_VALUE, ComputationTargetType.SECURITY, equitySecurity.getUniqueId());
      } catch (final Exception e) {
        s_logger.error(e.getMessage());
        return null;
      }
    }

    /**
     * Creates the data bundle required for equity total return swap pricing.
     * @param inputs The function inputs
     * @param matrix The FX matrix
     * @return The data bundle
     */
    protected EquityTrsDataBundle getDataBundle(final FunctionInputs inputs, final FXMatrix matrix) {
      final MulticurveProviderInterface curves = getMergedProviders(inputs, matrix);
      final double spotEquity = (Double) inputs.getValue(MARKET_VALUE);
      return new EquityTrsDataBundle(spotEquity, curves);
    }
  }
}
