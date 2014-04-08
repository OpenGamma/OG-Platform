/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.engine.value.ValueRequirementNames.JACOBIAN_BUNDLE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ProviderUtils;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.TradeConverter;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Base function for all pricing and risk functions that use the discounting construction method.
 */
public abstract class DiscountingFunction extends MultiCurvePricingFunction {

  /**
   * @param valueRequirements The value requirements, not null
   */
  public DiscountingFunction(final String... valueRequirements) {
    super(valueRequirements);
  }

  /**
   * Base compiled function for all pricing and risk functions that use the discounting curve construction method.
   */
  protected abstract class DiscountingCompiledFunction extends MultiCurveCompiledFunction {
    /** True if the result properties set the {@link ValuePropertyNames#CURRENCY} property */
    private final boolean _withCurrency;

    /**
     * @param tradeToDefinitionConverter Converts targets to definitions, not null
     * @param definitionToDerivativeConverter Converts definitions to derivatives, not null
     * @param withCurrency True if the result properties set the {@link ValuePropertyNames#CURRENCY} property
     */
    protected DiscountingCompiledFunction(final TradeConverter tradeToDefinitionConverter, final FixedIncomeConverterDataProvider definitionToDerivativeConverter, final boolean withCurrency) {
      super(tradeToDefinitionConverter, definitionToDerivativeConverter);
      _withCurrency = withCurrency;
    }

    @Override
    public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
      final Security security = target.getTrade().getSecurity();
      if (security instanceof ZeroCouponInflationSwapSecurity || security instanceof YearOnYearInflationSwapSecurity) {
        return false;
      }
      return super.canApplyTo(context, target);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties.Builder properties = createValueProperties()
          .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
          .withAny(CURVE_EXPOSURES);
      if (_withCurrency) {
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
          properties.with(CURRENCY, FinancialSecurityUtils.getCurrency(security).getCode());
        }
        // TODO: Handle the multiple currency case (SWAP_CROSS_CURRENCY) by returning a collection with more than one element
      }
      return Collections.singleton(properties);
    }

    @Override
    protected boolean requirementsSet(final ValueProperties constraints) {
      final Set<String> curveExposures = constraints.getValues(CURVE_EXPOSURES);
      if (curveExposures == null) {
        return false;
      }
      return true;
    }

    @Override
    protected Builder getCurveConstraints(final ComputationTarget target, final ValueProperties constraints) {
      return ValueProperties.with(PROPERTY_CURVE_TYPE, DISCOUNTING);
    }

    /**
     * Merges the multi-curve providers.
     *
     * @param inputs The function inputs, not null
     * @param matrix The FX matrix, not null
     * @return The merged providers
     */
    protected MulticurveProviderDiscount getMergedProviders(final FunctionInputs inputs, final FXMatrix matrix) {
      ArgumentChecker.notNull(inputs, "inputs");
      ArgumentChecker.notNull(matrix, "matrix");
      final Collection<MulticurveProviderDiscount> providers = new HashSet<>();
      for (final ComputedValue input : inputs.getAllValues()) {
        final String valueName = input.getSpecification().getValueName();
        if (CURVE_BUNDLE.equals(valueName)) {
          final ParameterProviderInterface generic = (ParameterProviderInterface) input.getValue();
          providers.add((MulticurveProviderDiscount) generic.getMulticurveProvider());
        }
      }
      final MulticurveProviderDiscount result = ProviderUtils.mergeDiscountingProviders(providers);
      return ProviderUtils.mergeDiscountingProviders(result, matrix);
    }

    /**
     * Merges the multi-curve blocks.
     *
     * @param inputs The function inputs, not null
     * @return The merged blocks
     */
    protected CurveBuildingBlockBundle getMergedCurveBuildingBlocks(final FunctionInputs inputs) {
      ArgumentChecker.notNull(inputs, "inputs");
      final CurveBuildingBlockBundle result = new CurveBuildingBlockBundle();
      for (final ComputedValue input : inputs.getAllValues()) {
        final String valueName = input.getSpecification().getValueName();
        if (valueName.equals(JACOBIAN_BUNDLE)) {
          result.addAll((CurveBuildingBlockBundle) input.getValue());
        }
      }
      return result;
    }

    /**
     * Gets the flag indicating whether or not the currency property is set.
     *
     * @return True if the currency property is set
     */
    protected boolean isWithCurrency() {
      return _withCurrency;
    }
  }
}
