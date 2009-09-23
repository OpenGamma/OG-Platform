/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class ResolveSecurityKeyToSecurityDefinition extends
    AnalyticValueDefinitionImpl<Security> {
  @SuppressWarnings("unchecked")
  public ResolveSecurityKeyToSecurityDefinition(SecurityKey key) {
     super(new KeyValuePair<String, Object>("TYPE", "RESOLVE_KEY_TO_SECURITY"),
           new KeyValuePair<String, Object>("SECURITY_KEY", key));
  }
}
