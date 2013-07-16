/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup.swap;

import com.opengamma.financial.security.lookup.SecurityValueProvider;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 *
 */
public class SwapRateProvider implements SecurityValueProvider<SwapSecurity> {

  @Override
  public Object getValue(SwapSecurity security) {
    return new RateVisitor().visit(security).getFirst();
  }
}
