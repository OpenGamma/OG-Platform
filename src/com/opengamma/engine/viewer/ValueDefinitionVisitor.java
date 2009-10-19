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
public interface ValueDefinitionVisitor<T> {
  public T visitAnalyticValueDefinitionImpl(AnalyticValueDefinitionImpl<?> definition);
}
