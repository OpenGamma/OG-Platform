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
public class SwapQuantityProvider implements SecurityValueProvider<SwapSecurity> {

  @Override
  public Double getValue(SwapSecurity security) {
    // the quantity is from the fixed leg or the pay leg for float/float swaps
    return new QuantityVisitor().visit(security).getFirst();
  }
}
