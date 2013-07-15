/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.SAMPLING_FUNCTION;
import static com.opengamma.engine.value.ValuePropertyNames.SAMPLING_PERIOD;
import static com.opengamma.engine.value.ValuePropertyNames.SCHEDULE_CALCULATOR;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;

/**
 * 
 */
public class CreditDefaultSwapIndexCS01PnLFunction extends CreditInstrumentCS01PnLFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof CreditDefaultSwapIndexSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> periodNames = constraints.getValues(SAMPLING_PERIOD);
    if (periodNames == null || periodNames.size() != 1) {
      return null;
    }
    final String samplingPeriod = periodNames.iterator().next();
    final Set<String> scheduleNames = constraints.getValues(SCHEDULE_CALCULATOR);
    if (scheduleNames == null || scheduleNames.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctionNames = constraints.getValues(SAMPLING_FUNCTION);
    if (samplingFunctionNames == null || samplingFunctionNames.size() != 1) {
      return null;
    }
    final CreditSecurityToIdentifierVisitor identifierVisitor = new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(context));
    final String spreadCurveName = security.accept(identifierVisitor).getUniqueId().getValue();
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(getBucketedCS01Requirement(security));
    requirements.add(getCreditSpreadCurveHTSRequirement(security, getCurvePrefix() + "_" + spreadCurveName, samplingPeriod));
    final Set<String> resultCurrencies = constraints.getValues(CURRENCY);
    if (resultCurrencies != null && resultCurrencies.size() == 1) {
      final ValueRequirement ccyConversionTSRequirement = getCurrencyConversionTSRequirement(position, currency, resultCurrencies);
      if (ccyConversionTSRequirement != null) {
        requirements.add(ccyConversionTSRequirement);
        requirements.add(new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetSpecification.NULL));
      }
    }
    return requirements;
  }

  @Override
  protected String getCurvePrefix() {
    return "CDS_INDEX";
  }
}
