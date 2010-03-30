/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.future.pricing;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.future.definition.FutureDefinition;
import com.opengamma.financial.model.future.definition.StandardFutureDataBundle;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 *
 */
public class CostOfCarryFutureModel implements FutureModel<StandardFutureDataBundle> {
  private static final Logger s_Log = LoggerFactory.getLogger(CostOfCarryFutureModel.class);

  @Override
  public GreekResultCollection getGreeks(final FutureDefinition definition, final StandardFutureDataBundle data, final Set<Greek> requiredGreeks) {
    if (definition == null)
      throw new IllegalArgumentException("Future definition was null");
    if (data == null)
      throw new IllegalArgumentException("Data bundle was null");
    if (requiredGreeks == null)
      throw new IllegalArgumentException("Set of required greeks was null");
    if (requiredGreeks.isEmpty())
      return new GreekResultCollection();
    if (!requiredGreeks.contains(Greek.FAIR_PRICE)) {
      s_Log.warn("Currently only fair price is calculated for futures");
      return new GreekResultCollection();
    }
    if (requiredGreeks.size() > 1)
      s_Log.warn("Currently only fair price is calculated for futures");
    final GreekResultCollection result = new GreekResultCollection();
    final double t = DateUtil.getDifferenceInYears(data.getDate(), definition.getExpiry().getExpiry());
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double q = data.getYield();
    final double s = data.getStorageCost();
    result.put(Greek.FAIR_PRICE, new SingleGreekResult((data.getSpot() + s) * Math.exp(t * (r - q))));
    return result;
  }
}
