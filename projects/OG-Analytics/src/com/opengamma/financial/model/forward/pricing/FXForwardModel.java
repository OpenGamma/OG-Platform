/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.pricing;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.forward.definition.FXForwardDataBundle;
import com.opengamma.financial.model.forward.definition.ForwardDefinition;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FXForwardModel implements ForwardModel<FXForwardDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(FXForwardModel.class);

  @Override
  public GreekResultCollection getGreeks(final ForwardDefinition definition, final FXForwardDataBundle data, final Set<Greek> requiredGreeks) {
    Validate.notNull(definition, "Forward definition");
    Validate.notNull(data, "Data bundle");
    Validate.notNull(requiredGreeks, "Required greeks");
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
    result.put(Greek.FAIR_PRICE, data.getSpot() * Math.exp(t * (r - rf)));
    return result;
  }

}
