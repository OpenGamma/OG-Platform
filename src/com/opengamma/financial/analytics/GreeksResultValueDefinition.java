/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.engine.viewer.VisitableValueDefinition;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class GreeksResultValueDefinition extends
    AnalyticValueDefinitionImpl<GreekResultCollection> implements VisitableValueDefinition {
  
  @SuppressWarnings("unchecked")
  public GreeksResultValueDefinition() {
    super(new KeyValuePair<String, Object>("TYPE", "GREEKS_RESULT"));
  }
  @SuppressWarnings("unchecked")
  public GreeksResultValueDefinition(SecurityKey securityKey) {
     super(new KeyValuePair<String, Object>("TYPE", "GREEKS_RESULT"),
           new KeyValuePair<String, Object>("SECURITY", securityKey));
  }
  
  public <E> E accept(FinancialValueDefinitionVisitor<E> visitor) {
    return visitor.visitGreeksResultValueDefinition(this);
  }
}
