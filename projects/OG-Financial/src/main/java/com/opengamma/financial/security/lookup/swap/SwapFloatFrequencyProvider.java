/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.lookup.SecurityValueProvider;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
*
*/
public class SwapFloatFrequencyProvider implements SecurityValueProvider<SwapSecurity> {

  @Override
  public Frequency getValue(SwapSecurity security) {
    // float leg frequency for fixed/float, receive leg frequency for float/float
    return new FrequencyVisitor().visit(security).getSecond();
  }
}
