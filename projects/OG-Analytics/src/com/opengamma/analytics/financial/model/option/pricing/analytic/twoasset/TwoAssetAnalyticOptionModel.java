/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.twoasset;

import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.OptionModel;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Base class for the analytic pricing of two-asset options.  
 * @param <T> Type of the option definition
 * @param <U> Type of the two asset data bundle
 */
public abstract class TwoAssetAnalyticOptionModel<T extends OptionDefinition, U extends StandardTwoAssetOptionDataBundle> implements OptionModel<T, U> {
  private static final Logger s_logger = LoggerFactory.getLogger(TwoAssetAnalyticOptionModel.class);

  /**
   * 
   * @param definition The option definition
   * @return A function that prices the option given a {@link StandardTwoAssetOptionDataBundle} or descendant class
   */
  public abstract Function1D<U, Double> getPricingFunction(T definition);

  /**
   * Note that currently the only greek that can be calculated is the fair price. If other greeks are requested a warning is given and
   * nothing is added to the result
   * @param definition The option definition
   * @param data The type of the data
   * @param requiredGreeks The greeks that are to be calculated
   * @return A {@link GreekResultCollection} containing the results
   * @throws IllegalArgumentException If any of the arguments are null, or if the set of required greeks is empty
   */
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
