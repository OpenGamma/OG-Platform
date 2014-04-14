package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.DefaultTradeConverter;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Base function for all pricing and risk functions that use the interpolated construction method.
 */
public abstract class DiscountingInterpolatedFunction extends DiscountingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public DiscountingInterpolatedFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  /**
   * 
   */
  protected abstract class DiscountingInterpolatedCompiledFunction extends DiscountingCompiledFunction {

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property
     */
    protected DiscountingInterpolatedCompiledFunction(final DefaultTradeConverter tradeToDefinitionConverter, final FixedIncomeConverterDataProvider definitionToDerivativeConverter,
        final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter, withCurrency);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties().with(PROPERTY_CURVE_TYPE, InterpolatedDataProperties.CALCULATION_METHOD_NAME).withAny(CURVE_EXPOSURES);
      if (isWithCurrency()) {
        final Security security = target.getTrade().getSecurity();
        if (security instanceof SwapSecurity && InterestRateInstrumentType.isFixedIncomeInstrumentType((SwapSecurity) security) &&
            (InterestRateInstrumentType.getInstrumentTypeFromSecurity((SwapSecurity) security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          final SwapSecurity swapSecurity = (SwapSecurity) security;
          if (swapSecurity.getPayLeg().getNotional() instanceof InterestRateNotional) {
            final String currency = ((InterestRateNotional) swapSecurity.getPayLeg().getNotional()).getCurrency().getCode();
            properties.with(CURRENCY, currency);
            return Collections.singleton(properties);
          }
        } else if (security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity) {
          properties.with(CURRENCY, ((FinancialSecurity) security).accept(ForexVisitors.getPayCurrencyVisitor()).getCode());
        } else {
          properties.with(CURRENCY, FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode());
        }
        // TODO: Handle instruments with multiple currencies by returning a set with more items
      }
      return Collections.singleton(properties);
    }

    @Override
    protected Builder getCurveConstraints(final ComputationTarget target, final ValueProperties constraints) {
      return ValueProperties.with(PROPERTY_CURVE_TYPE, InterpolatedDataProperties.CALCULATION_METHOD_NAME); //DISCOUNTING);
    }
  }
}
