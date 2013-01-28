/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter.swap;

import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.web.analytics.blotter.CellValueProvider;

/**
*
*/
public class SwapQuantityProvider implements CellValueProvider<SwapSecurity> {

  @Override
  public Double getValue(SwapSecurity security) {
    // the quantity is from the fixed leg or the pay leg for float/float swaps
    return new QuantityVisitor().visit(security).getFirst();
  }
}
