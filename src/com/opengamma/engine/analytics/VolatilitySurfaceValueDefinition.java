/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.security.SecurityKey;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class VolatilitySurfaceValueDefinition extends
    AnalyticValueDefinitionImpl<VolatilitySurface> {
  
  @SuppressWarnings("unchecked")
  public VolatilitySurfaceValueDefinition() {
     super(new KeyValuePair<String, Object>("TYPE", "VOLATILITY_SURFACE"));
  }
  @SuppressWarnings("unchecked")
  public VolatilitySurfaceValueDefinition(SecurityKey securityKey) {
     super(new KeyValuePair<String, Object>("TYPE", "VOLATILITY_SURFACE"),
           new KeyValuePair<String, Object>("SECURITY", securityKey));
  }
}
