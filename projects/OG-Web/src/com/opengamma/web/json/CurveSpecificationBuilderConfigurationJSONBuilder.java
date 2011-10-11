/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.ArgumentChecker;


/**
 * Custom JSON builder to convert CurveSpecificationBuilderConfiguration to JSON object and back again
 */
public final class CurveSpecificationBuilderConfigurationJSONBuilder extends AbstractJSONBuilder<CurveSpecificationBuilderConfiguration> {

  /**
   * Singleton
   */
  public static final CurveSpecificationBuilderConfigurationJSONBuilder INSTANCE = new CurveSpecificationBuilderConfigurationJSONBuilder();
  
  /**
   * JSON template
   */
  private static final String TEMPLATE = createTemplate();
  
  /**
   * Restricted constructor
   */
  private CurveSpecificationBuilderConfigurationJSONBuilder() {
  }

  @Override
  public CurveSpecificationBuilderConfiguration fromJSON(String json) {
    ArgumentChecker.notNull(json, "JSON document");
    return fromJSON(CurveSpecificationBuilderConfiguration.class, json);
  }

  @Override
  public String toJSON(CurveSpecificationBuilderConfiguration object) {
    ArgumentChecker.notNull(object, "curveSpecificationBuilderConfiguration");
    return toJSON(object, CurveSpecificationBuilderConfiguration.class);
  }


  private static String createTemplate() {
    return null;
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

}
