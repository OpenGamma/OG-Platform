/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.EqyOptBjerksundStenslandPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the present value of an equity index or equity option using the Bjerksund-Stensland (2002) formula.
 * See {@link ListedEquityOptionBjerksundStenslandFunction}
 */
public class ListedEquityOptionBjerksundStenslandPresentValueFunction extends ListedEquityOptionBjerksundStenslandFunction {

  /** The Bjerksund-Stensland present value calculator */
  private static final EqyOptBjerksundStenslandPresentValueCalculator s_calculator = EqyOptBjerksundStenslandPresentValueCalculator.getInstance();

  /** Default constructor */
  public ListedEquityOptionBjerksundStenslandPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final double pv = derivative.accept(s_calculator, market);
    return Collections.singleton(new ComputedValue(resultSpec, pv));
  }

}
