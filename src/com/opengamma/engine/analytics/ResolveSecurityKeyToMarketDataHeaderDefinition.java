/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Map;

import com.opengamma.engine.security.SecurityKey;
import com.opengamma.engine.viewer.ValueDefinitionVisitor;
import com.opengamma.engine.viewer.VisitableValueDefinition;
import com.opengamma.financial.securities.DataSource;
import com.opengamma.util.KeyValuePair;

/**
 * 
 *
 * @author jim
 */
public class ResolveSecurityKeyToMarketDataHeaderDefinition extends
    AnalyticValueDefinitionImpl<Map<String, Double>> implements VisitableValueDefinition {
  @SuppressWarnings("unchecked")
  public ResolveSecurityKeyToMarketDataHeaderDefinition(SecurityKey key) {
    super(new KeyValuePair<String, Object>("TYPE", "MARKET_DATA_HEADER"),
          new KeyValuePair<String, Object>("SECURITY_KEY", key),
          new KeyValuePair<String, Object>("DATA_SOURCE", "BLOOMBERG"));
 }
  @SuppressWarnings("unchecked")
  public ResolveSecurityKeyToMarketDataHeaderDefinition(SecurityKey key, DataSource dataSource) {
     super(new KeyValuePair<String, Object>("TYPE", "MARKET_DATA_HEADER"),
           new KeyValuePair<String, Object>("SECURITY_KEY", key),
           new KeyValuePair<String, Object>("DATA_SOURCE", dataSource.getName()));
  }
  
  public String getName() {
    return "Resolve "+getValue("SECURITY_KEY")+" to live data from "+getValue("DATA_SOURCE");
    
  }
  
  @Override
  public <E> E accept(ValueDefinitionVisitor<E> visitor) {
    return visitor.visitResolveSecurityKeyToMarketDataHeaderDefinition(this);
  }
}
