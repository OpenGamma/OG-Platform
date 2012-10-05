/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert FXForwardCurveDefinition to a JSON object and back again 
 */
public final class FXForwardCurveDefinitionJSONBuilder extends AbstractJSONBuilder<FXForwardCurveDefinition> {
  /** Singleton */
  public static final FXForwardCurveDefinitionJSONBuilder INSTANCE = new FXForwardCurveDefinitionJSONBuilder();
  /** JSON template */
  private static final String TEMPLATE = createTemplate();
  
  /**
   * Private constructor
   */
  private FXForwardCurveDefinitionJSONBuilder() {
  }
  
  @Override
  public FXForwardCurveDefinition fromJSON(String json) {
    ArgumentChecker.notNull(json, "JSON document");
    return fromJSON(FXForwardCurveDefinition.class, json);
  }

  @Override
  public String toJSON(FXForwardCurveDefinition object) {
    ArgumentChecker.notNull(object, "FXForwardCurveDefinition");
    return fudgeToJson(object);
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

  private static String createTemplate() {
    return null;
  }
}
