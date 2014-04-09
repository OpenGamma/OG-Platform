/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_EXPOSURES;
import static com.opengamma.engine.value.ValueRequirementNames.ASSET_LEG_PV;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.DISCOUNTING;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_CURVE_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.bond.calculator.BondTrsAssetLegPresentValueCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of the asset leg of a bond total return swap security.
 */
public class BondTotalReturnSwapAssetLegPVFunction extends BondTotalReturnSwapFunction {
  /** The calculator */
  private static final InstrumentDerivativeVisitor<IssuerProviderInterface, MultipleCurrencyAmount> CALCULATOR =
      BondTrsAssetLegPresentValueCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#ASSET_LEG_PV}.
   */
  public BondTotalReturnSwapAssetLegPVFunction() {
    super(ASSET_LEG_PV);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BondTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(ASSET_LEG_PV, target.toSpecification(), properties);
        final IssuerProviderInterface data = (IssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
        final MultipleCurrencyAmount pv = derivative.accept(CALCULATOR, data);
        final String expectedCurrency = spec.getProperty(CURRENCY);
        if (pv.size() != 1 || !(expectedCurrency.equals(pv.getCurrencyAmounts()[0].getCurrency().getCode()))) {
          throw new OpenGammaRuntimeException("Expecting a single result in " + expectedCurrency);
        }
        return Collections.singleton(new ComputedValue(spec, pv.getCurrencyAmounts()[0].getAmount()));
      }

      @SuppressWarnings("synthetic-access")
      @Override
      protected Collection<ValueProperties.Builder> getResultProperties(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final BondTotalReturnSwapSecurity security = (BondTotalReturnSwapSecurity) target.getTrade().getSecurity();
        final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(compilationContext);
        final BondSecurity bond = (BondSecurity) securitySource.getSingle(security.getAssetId().toBundle());
        final ValueProperties.Builder properties = createValueProperties()
            .with(PROPERTY_CURVE_TYPE, DISCOUNTING)
            .withAny(CURVE_EXPOSURES)
            .with(CURRENCY, bond.getCurrency().getCode());
        return Collections.singleton(properties);
      }

    };
  }

}
