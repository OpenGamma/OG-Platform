/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing;

import java.util.Set;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;

/**
 * 
 * @param <T>
 * @param <U>
 */
public interface OptionModel<T extends OptionDefinition, U> {

  GreekResultCollection getGreeks(T definition, U data, Set<Greek> requiredGreeks);

}
