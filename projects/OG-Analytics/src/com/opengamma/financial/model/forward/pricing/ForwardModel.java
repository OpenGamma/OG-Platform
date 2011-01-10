/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.pricing;

import java.util.Set;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.forward.definition.ForwardDataBundle;
import com.opengamma.financial.model.forward.definition.ForwardDefinition;

/**
 * @param <T> Type of the data bundle
 */
public interface ForwardModel<T extends ForwardDataBundle> {

  GreekResultCollection getGreeks(ForwardDefinition definition, T data, Set<Greek> requiredGreeks);
}
