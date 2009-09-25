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
  public VolatilitySurfaceValueDefinition(SecurityKey securityKey) {
     super(new KeyValuePair<String, Object>("TYPE", "VOLATILITY_SURFACE"),
           new KeyValuePair<String, Object>("SECURITY", securityKey));
  }
  
  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof VolatilitySurfaceValueDefinition)) {
      return false;
    }
    VolatilitySurfaceValueDefinition other = (VolatilitySurfaceValueDefinition) obj;
    if (getValue("SECURITY") == null || other.getValue("SECURITY") == null) {
      System.out.println("Returning true");
      return true;
    }
    return AnalyticValueDefinitionComparator.equals(this, (AnalyticValueDefinition<?>)obj);
  }

  @Override
  public int hashCode() {
    return 8;
    //return AnalyticValueDefinitionComparator.hashCode(this);
  }
}
