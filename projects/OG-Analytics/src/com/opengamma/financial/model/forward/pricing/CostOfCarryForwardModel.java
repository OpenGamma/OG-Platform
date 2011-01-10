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
import com.opengamma.financial.model.forward.definition.ForwardDefinition;
import com.opengamma.financial.model.forward.definition.StandardForwardDataBundle;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class CostOfCarryForwardModel implements ForwardModel<StandardForwardDataBundle> {
  private static final Logger s_logger = LoggerFactory.getLogger(CostOfCarryForwardModel.class);

  @Override
  public GreekResultCollection getGreeks(final ForwardDefinition definition, final StandardForwardDataBundle data, final Set<Greek> requiredGreeks) {
    Validate.notNull(definition, "Forward definition");
    Validate.notNull(data, "Forward data bundle");
    Validate.notNull(requiredGreeks, "Required greeks");
    if (requiredGreeks.isEmpty()) {
      return new GreekResultCollection();
    }
    if (!requiredGreeks.contains(Greek.FAIR_PRICE)) {
      s_logger.warn("Currently only fair price is calculated for forwards");
      return new GreekResultCollection();
    }
    if (requiredGreeks.size() > 1) {
      s_logger.warn("Currently only fair price is calculated for forwards");
    }
    final GreekResultCollection result = new GreekResultCollection();
    final double t = DateUtil.getDifferenceInYears(data.getDate(), definition.getExpiry().getExpiry());
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double q = data.getYield();
    final double s = data.getStorageCost();
    result.put(Greek.FAIR_PRICE, (data.getSpot() + s) * Math.exp(t * (r - q)));
    return result;
  }
}
