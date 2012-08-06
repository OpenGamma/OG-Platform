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
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Produces the current value of the underlying index, according to the market data
 */
public class EquityIndexVanillaBarrierOptionSpotIndexFunction extends EquityIndexVanillaBarrierOptionFunction {

  public EquityIndexVanillaBarrierOptionSpotIndexFunction() {
    super(ValueRequirementNames.SPOT);
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    return super.createValueProperties(target).with(ValuePropertyNames.CURRENCY, getEquityBarrierOptionSecurity(target).getCurrency().getCode());
  }

  @Override
  protected Object computeValues(Set<EquityIndexOption> vanillaOptions, StaticReplicationDataBundle market) {
    EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    return model.spotIndexValue(market);
  }

}
