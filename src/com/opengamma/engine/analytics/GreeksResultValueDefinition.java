/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.security.SecurityKey;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class GreeksResultValueDefinition extends
    AnalyticValueDefinitionImpl<GreekResultCollection> {
  
  @SuppressWarnings("unchecked")
  public GreeksResultValueDefinition() {
    super(new KeyValuePair<String, Object>("TYPE", "GREEKS_RESULT"));
  }
  @SuppressWarnings("unchecked")
  public GreeksResultValueDefinition(SecurityKey securityKey) {
     super(new KeyValuePair<String, Object>("TYPE", "GREEKS_RESULT"),
           new KeyValuePair<String, Object>("SECURITY", securityKey));
  }
}
