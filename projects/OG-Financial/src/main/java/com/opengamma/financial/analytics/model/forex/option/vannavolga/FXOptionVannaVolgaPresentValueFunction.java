/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.vannavolga;

import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.CALL_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.PUT_CURVE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaVannaVolgaMethod;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureVannaVolgaDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FXOptionVannaVolgaPresentValueFunction extends FXOptionVannaVolgaSingleValuedFunction {
  private static final ForexOptionVanillaVannaVolgaMethod CALCULATOR = ForexOptionVanillaVannaVolgaMethod.getInstance();

  public FXOptionVannaVolgaPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final Object baseQuotePairsObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
    if (baseQuotePairsObject == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair data");
    }
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);
    final String deltaName = desiredValue.getConstraint(PROPERTY_OTM_DELTA);
    final String[] allCurveNames = getCurveNames(putCurrency, putCurveName, callCurrency, callCurveName, baseQuotePairs);
    final SmileDeltaTermStructureVannaVolgaDataBundle smiles = getSmiles(putCurrency, callCurrency, allCurveNames, baseQuotePairs, deltaName, inputs);
    final ForexOptionVanilla fxOption = (ForexOptionVanilla) getDerivative(security, allCurveNames, baseQuotePairs, now);
    final MultipleCurrencyAmount pv = CALCULATOR.presentValue(fxOption, smiles);
    ArgumentChecker.isTrue(pv.size() == 1, "result size must be one; have {}", pv.size());
    final CurrencyAmount ca = pv.getCurrencyAmounts()[0];
    final double amount = ca.getAmount();
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    final ValueSpecification spec = getSpecification(target, desiredValue, baseQuotePair);
    return Collections.singleton(new ComputedValue(spec, amount));
  }
}
