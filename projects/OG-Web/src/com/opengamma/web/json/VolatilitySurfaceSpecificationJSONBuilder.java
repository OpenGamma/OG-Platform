/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert VolatilitySurfaceSpecification to JSON object and back again
 */
public final class VolatilitySurfaceSpecificationJSONBuilder extends AbstractJSONBuilder<VolatilitySurfaceSpecification> {
   
  
  /**
   * Singleton
   */
  public static final VolatilitySurfaceSpecificationJSONBuilder INSTANCE = new VolatilitySurfaceSpecificationJSONBuilder();
  
  /**
   * JSON template
   */
  private static final String TEMPLATE = createTemplate();
  
  /**
   * Restricted constructor
   */
  private VolatilitySurfaceSpecificationJSONBuilder() {
  }

  private static String createTemplate() {
    return null;
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

  @Override
  public String toJSON(VolatilitySurfaceSpecification object) {
    ArgumentChecker.notNull(object, "VolatilitySurfaceSpecification");
    return fudgeToJson(object);
  }

  @Override
  public VolatilitySurfaceSpecification fromJSON(String json) {
    ArgumentChecker.notNull(json, "JSON document");
    return fromJSON(VolatilitySurfaceSpecification.class, json);
  }

}
