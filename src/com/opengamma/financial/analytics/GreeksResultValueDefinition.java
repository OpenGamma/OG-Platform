/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collection;

import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.AnalyticValueDefinitionImpl;
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
  public GreeksResultValueDefinition(String securityIdentityKey) {
     super(new KeyValuePair<String, Object>("TYPE", "GREEKS_RESULT"),
           new KeyValuePair<String, Object>("SECURITY", securityIdentityKey));
  }
//  public GreeksResultValueDefinition(Security securityKey, Position position) {
//    super(new KeyValuePair<String, Object>("TYPE", "GREEKS_RESULT"),
//          new KeyValuePair<String, Object>("SECURITY", securityKey),
//          new KeyValuePair<String, Object>("POSITION", position));
// }
  @SuppressWarnings("unchecked")
  public GreeksResultValueDefinition(Collection<Position> positions) {
     super(new KeyValuePair<String, Object>("TYPE", "GREEKS_RESULT"),
           new KeyValuePair<String, Object>("POSITIONS", positions));
  }
  public <E> E accept(FinancialValueDefinitionVisitor<E> visitor) {
    return visitor.visitGreeksResultValueDefinition(this);
  }
}
