/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter.swap;

import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.analytics.blotter.ValueProvider;

/**
 *
 */
public class SwapIndexProvider implements ValueProvider<SwapSecurity> {

  @Override
  public String getValue(SwapSecurity security) {
    Pair<ExternalId,ExternalId> indices = new IndexVisitor().visit(security);
    // float index for fixed/float, receive index for float/float
    return indices.getSecond().getValue();
  }
}
