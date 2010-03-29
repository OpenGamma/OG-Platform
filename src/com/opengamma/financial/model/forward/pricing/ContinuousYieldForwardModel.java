/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.pricing;

import java.util.Set;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.forward.definition.ContinuousYieldForwardDataBundle;
import com.opengamma.financial.model.forward.definition.ForwardDefinition;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 *
 */
public class ContinuousYieldForwardModel implements ForwardModel<ContinuousYieldForwardDataBundle> {

  /*
   * (non-Javadoc)
   * 
   * @see
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * com.opengamma.financial.model.forward.pricing.ForwardModel#getGreeks(com.opengamma.financial.model.forward.definition
   * .ForwardDefinition, com.opengamma.financial.model.forward.definition.ForwardDataBundle, java.util.Set)
   */
  @Override
  public GreekResultCollection getGreeks(final ForwardDefinition definition, final ContinuousYieldForwardDataBundle data, final Set<Greek> requiredGreeks) {
    if (definition == null)
      throw new IllegalArgumentException("Forward definition was null");
    if (data == null)
      throw new IllegalArgumentException("Data bundle was null");
    if (requiredGreeks == null)
      throw new IllegalArgumentException("Set of required greeks was null");
    final GreekResultCollection result = new GreekResultCollection();
    final double t = DateUtil.getDifferenceInYears(data.getDate(), definition.getExpiry().getExpiry());
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double d = data.getYield();
    result.put(Greek.FAIR_PRICE, new SingleGreekResult(data.getSpot() * Math.exp(t * (r - d))));
    return result;
  }
}
