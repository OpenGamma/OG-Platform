/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
 * @author emcleod
 *
 */
public interface FutureModel<T extends FutureDataBundle> {

  public GreekResultCollection getGreeks(FutureDefinition definition, T data, Set<Greek> requiredGreeks);
}
