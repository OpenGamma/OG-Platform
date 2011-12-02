/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.forex.calculator.PresentValueBlackForexCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexSingleBarrierOptionPresentValueFunction extends ForexSingleBarrierOptionFunction {
  private static final PresentValueBlackForexCalculator CALCULATOR = PresentValueBlackForexCalculator.getInstance();

  public ForexSingleBarrierOptionPresentValueFunction(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName,
      final String surfaceName) {
    super(putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxSingleBarrierOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxSingleBarrierOption, data);
    Validate.isTrue(result.size() == 1);
    CurrencyAmount ca = result.getCurrencyAmounts()[0];
    Currency ccy = ca.getCurrency();
    double amount = ca.getAmount();
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutFundingCurveName()).with(ValuePropertyNames.RECEIVE_CURVE, getCallFundingCurveName())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, amount));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    FXBarrierOptionSecurity security = (FXBarrierOptionSecurity) target.getSecurity();
    Currency putCurrency = security.getPutCurrency();
    Currency callCurrency = security.getCallCurrency();
    Currency ccy;
    if (ForexUtils.isBaseCurrency(putCurrency, callCurrency)) {
      ccy = putCurrency;
    } else {
      ccy = callCurrency;
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutFundingCurveName()).with(ValuePropertyNames.RECEIVE_CURVE, getCallFundingCurveName())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties));
  }
}
