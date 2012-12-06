/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.analytics.financial.commodity.calculator.CommodityFutureOptionBAWGreeksCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public class CommodityFutureOptionBAWGreeksFunction extends CommodityFutureOptionBlackFunction {
  private static final String[] GREEK_NAMES = new String[] {
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_DUAL_DELTA,
    ValueRequirementNames.VALUE_RHO,
    ValueRequirementNames.VALUE_CARRY_RHO,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.VALUE_THETA
  };
  private static final Greek[] GREEKS = new Greek[] {
    Greek.DELTA,
    Greek.DUAL_DELTA,
    Greek.RHO,
    Greek.CARRY_RHO,
    Greek.VEGA,
    Greek.THETA
  };

  public CommodityFutureOptionBAWGreeksFunction() {
    super(GREEK_NAMES);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final Set<ValueRequirement> desiredValues,
      final ComputationTarget target) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final GreekResultCollection greeks = derivative.accept(CommodityFutureOptionBAWGreeksCalculator.getInstance(), market);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueProperties properties = createResultProperties(desiredValue.getConstraints());
    final Set<ComputedValue> result = new HashSet<ComputedValue>();
    for (int i = 0; i < GREEKS.length; i++) {
      final ValueSpecification spec = new ValueSpecification(GREEK_NAMES[i], targetSpec, properties);
      final double greek = greeks.get(GREEKS[i]);
      result.add(new ComputedValue(spec, greek));
    }
    return result;
  }

}
