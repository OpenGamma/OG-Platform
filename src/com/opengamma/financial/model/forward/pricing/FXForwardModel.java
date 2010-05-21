/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.pricing;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.greeks.SingleGreekResult;
import com.opengamma.financial.model.forward.definition.FXForwardDataBundle;
import com.opengamma.financial.model.forward.definition.ForwardDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;

public class FXForwardModel implements ForwardModel<FXForwardDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardModel.class);

  @Override
  public GreekResultCollection getGreeks(final ForwardDefinition definition, final FXForwardDataBundle data,
      final Set<Greek> requiredGreeks) {
    ArgumentChecker.notNull(definition, "Forward definition");
    ArgumentChecker.notNull(data, "Data bundle");
    ArgumentChecker.notNull(requiredGreeks, "Required greeks");

    if (requiredGreeks.isEmpty()) {
      return new GreekResultCollection();
    }
    if (!requiredGreeks.contains(Greek.FAIR_PRICE)) {
      s_logger.warn("Currently only fair price is calculated for FX forwards");
      return new GreekResultCollection();
    }
    if (requiredGreeks.size() > 1) {
      s_logger.warn("Currently only fair price is calculated for FX forwards");
    }
    final GreekResultCollection result = new GreekResultCollection();
    final double t = DateUtil.getDifferenceInYears(data.getDate(), definition.getExpiry().getExpiry());
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double rf = data.getForeignCurve().getInterestRate(t);
    result.put(Greek.FAIR_PRICE, new SingleGreekResult(data.getSpot() * Math.exp(t * (r - rf))));
    return result;
  }

}
