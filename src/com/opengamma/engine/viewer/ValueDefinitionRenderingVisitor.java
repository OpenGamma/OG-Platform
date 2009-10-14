/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;
import com.opengamma.engine.analytics.DiscountCurveValueDefinition;
import com.opengamma.engine.analytics.GreeksResultValueDefinition;
import com.opengamma.engine.analytics.ResolveSecurityKeyToMarketDataFieldDefinition;
import com.opengamma.engine.analytics.ResolveSecurityKeyToMarketDataHeaderDefinition;
import com.opengamma.engine.analytics.ResolveSecurityKeyToSecurityDefinition;
import com.opengamma.engine.analytics.VolatilitySurfaceValueDefinition;

/**
 * 
 *
 * @author jim
 */
public class ValueDefinitionRenderingVisitor implements
    ValueDefinitionVisitor<String> {

  @Override
  public String visitAnalyticValueDefinitionImpl(
      AnalyticValueDefinitionImpl<?> definition) {
    StringBuilder sb = new StringBuilder();
    for (String key : definition.getKeys()) {
      sb.append(key);
      sb.append("=");
      sb.append(definition.getValue(key));
      sb.append(", ");
    }
    if (sb.length() > 2) {
      sb.delete(sb.length() - 2, sb.length());
    }
    return "General Definition: "+sb.toString();
  }

  @Override
  public String visitDiscountCurveValueDefinition(
      DiscountCurveValueDefinition definition) {
    return "Build discount curve for "+definition.getValue("CURRENCY");
  }

  @Override
  public String visitGreeksResultValueDefinition(
      GreeksResultValueDefinition definition) {
    return "Calculate Greeks for "+definition.getValue("SECURITY");
  }

  @Override
  public String visitResolveSecurityKeyToMarketDataFieldDefinition(
      ResolveSecurityKeyToMarketDataFieldDefinition definition) {
    return "Resolve "+definition.getValue("SECURITY_KEY")+" to field "+definition.getValue("FIELD_NAME");
  }

  @Override
  public String visitResolveSecurityKeyToMarketDataHeaderDefinition(ResolveSecurityKeyToMarketDataHeaderDefinition definition) {
    return "Resolve "+definition.getValue("SECURITY_KEY")+" to live data from "+definition.getValue("DATA_SOURCE");
  }

  @Override
  public String visitResolveSecurityKeyToSecurityDescriptionDefinition(ResolveSecurityKeyToSecurityDefinition definition) {
    return "Resolve "+definition.getValue("SECURITY_KEY")+" to security definition";
  }

  @Override
  public String visitVolatilitySurfaceValueDefinition(VolatilitySurfaceValueDefinition definition) {
    return "Build volatility curve for "+definition.getValue("SECURITY");
  }

}
