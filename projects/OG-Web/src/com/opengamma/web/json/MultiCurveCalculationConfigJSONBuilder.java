/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MultiCurveCalculationConfigJSONBuilder extends AbstractJSONBuilder<MultiCurveCalculationConfig> {
  /** Singleton */
  public static final MultiCurveCalculationConfigJSONBuilder INSTANCE = new MultiCurveCalculationConfigJSONBuilder();
  /** JSON template */
  private static final String TEMPLATE = createTemplate();
  
  @Override
  public MultiCurveCalculationConfig fromJSON(String json) {
    ArgumentChecker.notNull(json, "JSON document");
    return fromJSON(MultiCurveCalculationConfig.class, json);
  }

  @Override
  public String toJSON(MultiCurveCalculationConfig object) {
    ArgumentChecker.notNull(object, "MultiCurveCalculationConfig");
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
