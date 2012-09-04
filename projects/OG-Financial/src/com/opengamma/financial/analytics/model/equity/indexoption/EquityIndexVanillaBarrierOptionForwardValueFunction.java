/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Set;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * The <b>forward</b> value of the index, ie the fair strike of a forward agreement paying the index value at maturity,
 * as seen from the selected market data. <p>
 */
public class EquityIndexVanillaBarrierOptionForwardValueFunction extends EquityIndexVanillaBarrierOptionFunction {

  /**
   * @param requirementName
   */
  public EquityIndexVanillaBarrierOptionForwardValueFunction() {
    super(ValueRequirementNames.FORWARD);
  }

  @Override
  protected Object computeValues(Set<EquityIndexOption> vanillaOptions, StaticReplicationDataBundle market) {
    EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    final EquityIndexOption firstDerivative = vanillaOptions.iterator().next();
    return model.forwardIndexValue(firstDerivative, market); // All derivatives in the set share their forward
  }

}
