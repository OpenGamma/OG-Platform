/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.util.Iterator;
import java.util.SortedSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;


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
    JSONObject message;
    try {
      message = new JSONObject(fudgeToJson(object));
      message.put("tenors", new JSONArray(getTenors(object)));
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Error converting cpnverspecification builder configuration to JSON", ex);
    }
    return message.toString();
  }

  private String[] getTenors(final CurveSpecificationBuilderConfiguration curveSpec) {
    SortedSet<Tenor> allTenors = curveSpec.getAllTenors();
    String[] periods = new String[allTenors.size()];
    Iterator<Tenor> iterator = allTenors.iterator();
    for (int i = 0; i < allTenors.size(); i++) {
      periods[i] = iterator.next().getPeriod().toString();
    }
    return periods;
  }


  private static String createTemplate() {
    return null;
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

}
