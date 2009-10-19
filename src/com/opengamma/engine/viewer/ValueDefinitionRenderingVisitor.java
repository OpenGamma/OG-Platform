/**

 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import com.opengamma.engine.analytics.AnalyticValueDefinitionImpl;

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


}
