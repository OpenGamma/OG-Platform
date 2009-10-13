/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.financial.securities.Field;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class ResolveSecurityKeyToMarketDataFieldDefinition extends
    AnalyticValueDefinitionImpl<Security> {
  @SuppressWarnings("unchecked")
  public ResolveSecurityKeyToMarketDataFieldDefinition(SecurityKey key, Field field) {
     super(new KeyValuePair<String, Object>("TYPE", "RESOLVE_KEY_FIELD_TO_VALUE"),
           new KeyValuePair<String, Object>("SECURITY_KEY", key),
           new KeyValuePair<String, Object>("FIELD_NAME", field.getName()));
  }
  
  public String getName() {
    return "Resolve field "+getValue("FIELD_NAME")+" from "+getValue("SECURITY_KEY");
  }
}
