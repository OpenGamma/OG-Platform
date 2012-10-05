/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Custon JSON builder to convert FXForwardCurveSpecification to a JSON object and back again
 */
public final class FXForwardCurveSpecificationJSONBuilder extends AbstractJSONBuilder<FXForwardCurveSpecification> {
  /** Singleton */
  public static final FXForwardCurveSpecificationJSONBuilder INSTANCE = new FXForwardCurveSpecificationJSONBuilder();
  /** JSON template */
  private static final String TEMPLATE = createTemplate();
  
  /**
   * Private constructor
   */
  private FXForwardCurveSpecificationJSONBuilder() {
  }
  
  @Override
  public FXForwardCurveSpecification fromJSON(String json) {
    ArgumentChecker.notNull(json, "JSON document");    
    return fromJSON(FXForwardCurveSpecification.class, json);
  }

  @Override
  public String toJSON(FXForwardCurveSpecification object) {
    ArgumentChecker.notNull(object, "FXForwardCurveSpecification");
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
