/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.factory.tool;

import com.opengamma.component.factory.tool.ToolContextComponentFactory;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.tool.DataTrackingToolContext;

/**
 * 
 */
public class DataTrackingToolContextComponentFactory extends ToolContextComponentFactory {

  @Override
  protected ToolContext createToolContext() {
    return new DataTrackingToolContext();
  }
  
}
