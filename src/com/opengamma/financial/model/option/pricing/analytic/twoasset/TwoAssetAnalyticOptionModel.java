/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.twoasset;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <T> Type of the option definition
 * @param <U> Type of the two asset data bundle
 */
public abstract class TwoAssetAnalyticOptionModel<T extends OptionDefinition, U extends StandardTwoAssetOptionDataBundle> implements OptionModel<T, U> {
  private static final Logger s_logger = LoggerFactory.getLogger(TwoAssetAnalyticOptionModel.class);

  public abstract Function1D<U, Double> getPricingFunction(T definition);

  @Override
  public GreekResultCollection getGreeks(final T definition, final U data, final Set<Greek> requiredGreeks) {
    Validate.notNull(definition, "definition");
    Validate.notNull(data, "data");
    Validate.notNull(requiredGreeks, "required greeks");
    Validate.notEmpty(requiredGreeks, "required greeks");
    final Function1D<U, Double> pricingFunction = getPricingFunction(definition);
    final GreekResultCollection results = new GreekResultCollection();
    for (final Greek greek : requiredGreeks) {
      if (greek != Greek.FAIR_PRICE) {
        s_logger.warn("Can only calculate price for two-asset options, not calculating " + greek);
      } else {
        results.put(greek, pricingFunction.evaluate(data));
      }
    }
    return results;
  }

}
