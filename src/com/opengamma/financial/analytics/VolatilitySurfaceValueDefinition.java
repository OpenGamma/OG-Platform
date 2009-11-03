/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.viewer.VisitableValueDefinition;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class VolatilitySurfaceValueDefinition extends
    AnalyticValueDefinitionImpl<VolatilitySurface> implements VisitableValueDefinition {
  
  @SuppressWarnings("unchecked")
  public VolatilitySurfaceValueDefinition() {
     super(new KeyValuePair<String, Object>("TYPE", "VOLATILITY_SURFACE"));
  }
  @SuppressWarnings("unchecked")
  public VolatilitySurfaceValueDefinition(String identityKey) {
     super(new KeyValuePair<String, Object>("TYPE", "VOLATILITY_SURFACE"),
           new KeyValuePair<String, Object>("SECURITY", identityKey));
  }
  
  public <T> T accept(FinancialValueDefinitionVisitor<T> visitor) {
    return visitor.visitVolatilitySurfaceValueDefinition(this);
  }
}
