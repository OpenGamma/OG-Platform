/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.future.pricing;

import java.util.Set;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.future.definition.FutureDataBundle;
import com.opengamma.financial.model.future.definition.FutureDefinition;

/**
 * @param <T> Type of the data bundle
 */
public interface FutureModel<T extends FutureDataBundle> {

  GreekResultCollection getGreeks(FutureDefinition definition, T data, Set<Greek> requiredGreeks);
}
