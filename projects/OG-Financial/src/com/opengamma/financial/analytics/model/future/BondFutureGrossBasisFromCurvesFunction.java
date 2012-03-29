/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.calculator.BondFutureGrossBasisFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.financial.analytics.model.bond.BondFunction;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondFutureGrossBasisFromCurvesFunction extends BondFutureFromCurvesFunction {
  private static final BondFutureGrossBasisFromCurvesCalculator CALCULATOR = BondFutureGrossBasisFromCurvesCalculator.getInstance();

  public BondFutureGrossBasisFromCurvesFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName, ValueRequirementNames.GROSS_BASIS, BondFunction.FROM_CURVES_METHOD);
  }

  public BondFutureGrossBasisFromCurvesFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName, ValueRequirementNames.GROSS_BASIS, BondFunction.FROM_CURVES_METHOD);
  }

  @Override
  protected Set<ComputedValue> calculate(final com.opengamma.financial.security.future.BondFutureSecurity security, final BondFuture bondFuture, final YieldCurveBundle data,
      final ComputationTarget target) {
    final List<BondFutureDeliverable> deliverables = security.getBasket();
    final int n = deliverables.size();
    final double[] grossBasis = CALCULATOR.visit(bondFuture, data);
    if (grossBasis.length != n) {
      throw new OpenGammaRuntimeException("Do not have a gross basis for every deliverable: should never happen");
    }
    final String[] label = new String[n];
    for (int i = 0; i < n; i++) {
      label[i] = deliverables.get(i).getIdentifiers().getValue(SecurityUtils.BLOOMBERG_BUID); //TODO get a better label
    }
    final StringLabelledMatrix1D result = new StringLabelledMatrix1D(label, grossBasis);
    return Sets.newHashSet(new ComputedValue(getResultSpec(target), result));
  }
}
