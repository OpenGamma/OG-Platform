/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;

/**
 * Custom JSON builder to convert VolatilitySurfaceDefinition to JSON object and back again
 */
public final class VolatilitySurfaceDefinitionJSONBuilder extends AbstractJSONBuilder<VolatilitySurfaceDefinition> {
  
  /**
   * Singleton
   */
  public static final VolatilitySurfaceDefinitionJSONBuilder INSTANCE = new VolatilitySurfaceDefinitionJSONBuilder();
  
  /**
   * JSON template
   */
  private static final String TEMPLATE = createTemplate();
  
  /**
   * Restricted constructor
   */
  private VolatilitySurfaceDefinitionJSONBuilder() {
  }

  private static String createTemplate() {
    return null;
  }

  @Override
  public VolatilitySurfaceDefinition fromJSON(String json) {
    return fromJSON(VolatilitySurfaceDefinition.class, json);
  }

  @Override
  public String toJSON(VolatilitySurfaceDefinition object) {
    return fudgeToJson(object);
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

}
