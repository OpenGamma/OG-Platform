/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.security.Currency;
import com.opengamma.engine.security.Security;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class DiscountCurveValueDefinition extends
    AnalyticValueDefinitionImpl<Security> {
  @SuppressWarnings("unchecked")
  public DiscountCurveValueDefinition(Currency currency) {
     super(new KeyValuePair<String, Object>("TYPE", "DISCOUNT_CURVE"),
           new KeyValuePair<String, Object>("CURRENCY", currency.getISOCode()));
  }
}
