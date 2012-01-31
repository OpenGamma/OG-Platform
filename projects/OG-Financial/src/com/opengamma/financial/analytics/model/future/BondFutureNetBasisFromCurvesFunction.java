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
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.financial.analytics.model.bond.BondFunction;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.calculator.BondFutureNetBasisFromCurvesCalculator;
import com.opengamma.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondFutureNetBasisFromCurvesFunction extends BondFutureFromCurvesFunction {
  private static final BondFutureNetBasisFromCurvesCalculator CALCULATOR = BondFutureNetBasisFromCurvesCalculator.getInstance();

  public BondFutureNetBasisFromCurvesFunction(final String currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName, ValueRequirementNames.NET_BASIS, BondFunction.FROM_CURVES_METHOD);
  }

  public BondFutureNetBasisFromCurvesFunction(final Currency currency, final String creditCurveName, final String riskFreeCurveName) {
    super(currency, creditCurveName, riskFreeCurveName, ValueRequirementNames.NET_BASIS, BondFunction.FROM_CURVES_METHOD);
  }

  @Override
  protected Set<ComputedValue> calculate(final com.opengamma.financial.security.future.BondFutureSecurity security, final BondFuture bondFuture, final YieldCurveBundle data,
      final ComputationTarget target) {
    final List<BondFutureDeliverable> deliverables = security.getBasket();
    final int n = deliverables.size();
    final double[] netBasis = CALCULATOR.visit(bondFuture, data);
    if (netBasis.length != n) {
      throw new OpenGammaRuntimeException("Do not have a net basis for every deliverable: should never happen");
    }
    final String[] label = new String[n];
    for (int i = 0; i < n; i++) {
      label[i] = deliverables.get(i).getIdentifiers().getValue(SecurityUtils.BLOOMBERG_BUID);
    }
    final StringLabelledMatrix1D result = new StringLabelledMatrix1D(label, netBasis);
    return Sets.newHashSet(new ComputedValue(getResultSpec(target), result));
  }
}
