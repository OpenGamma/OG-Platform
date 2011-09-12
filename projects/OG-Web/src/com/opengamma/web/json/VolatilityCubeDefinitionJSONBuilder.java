/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Custom JSON builder to convert VolatilityCubeDefinition to JSON object and back again
 */
public final class VolatilityCubeDefinitionJSONBuilder extends AbstractJSONBuilder<VolatilityCubeDefinition> {
   
  private static final String SWAP_TENORS_FIELD = "swapTenors";
  private static final String OPTION_EXPIRIES_FIELD = "optionExpiries";
  private static final String RELATIVE_STRIKES_FIELD = "relativeStrikes";
  
  /**
   * Singleton
   */
  public static final VolatilityCubeDefinitionJSONBuilder INSTANCE = new VolatilityCubeDefinitionJSONBuilder();
  
  /**
   * JSON template
   */
  private static final String TEMPLATE = createTemplate();
  
  /**
   * Restricted constructor
   */
  private VolatilityCubeDefinitionJSONBuilder() {
  }

  private static String createTemplate() {
    return null;
  }

  @Override
  public VolatilityCubeDefinition fromJSON(String json) {
    ArgumentChecker.notNull(json, "JSON document");
    
    VolatilityCubeDefinition volatilityCubeDefinition = new VolatilityCubeDefinition();
    try {
      JSONObject jsonObject = new JSONObject(json);
      if (jsonObject.opt(UNIQUE_ID_FIELD) != null) {
        volatilityCubeDefinition.setUniqueId(UniqueId.parse(jsonObject.getString(UNIQUE_ID_FIELD)));
      }
      if (jsonObject.opt(SWAP_TENORS_FIELD) != null) {
        JSONArray jsonArray = jsonObject.getJSONArray(SWAP_TENORS_FIELD);
        volatilityCubeDefinition.setSwapTenors(toTenorList(jsonArray));
      }
      if (jsonObject.opt(OPTION_EXPIRIES_FIELD) != null) {
        JSONArray jsonArray = jsonObject.getJSONArray(OPTION_EXPIRIES_FIELD);
        volatilityCubeDefinition.setOptionExpiries(toTenorList(jsonArray));
      }
      if (jsonObject.opt(RELATIVE_STRIKES_FIELD) != null) {
        JSONArray jsonArray = jsonObject.getJSONArray(RELATIVE_STRIKES_FIELD);
        volatilityCubeDefinition.setRelativeStrikes(toDoubleList(jsonArray));
      }
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("Unable to create CurveSpecificationBuilderConfiguration", ex);
    }
    return volatilityCubeDefinition;
  }

  @Override
  public String toJSON(VolatilityCubeDefinition volatilityCubeDefinition) {
    ArgumentChecker.notNull(volatilityCubeDefinition, "volatilityCubeDefinition");
    JSONObject jsonObject = new JSONObject();
    try {
      buildClassName(VolatilityCubeDefinition.class, jsonObject);
      buildUID(volatilityCubeDefinition.getUniqueId(), jsonObject);
      buildTenors(volatilityCubeDefinition.getSwapTenors(), jsonObject, SWAP_TENORS_FIELD);
      buildTenors(volatilityCubeDefinition.getOptionExpiries(), jsonObject, OPTION_EXPIRIES_FIELD);
      buildRelativeStrikes(volatilityCubeDefinition.getRelativeStrikes(), jsonObject);
    } catch (JSONException ex) {
      throw new OpenGammaRuntimeException("unable to convert CurveSpecificationBuilderConfiguration to JSON", ex);
    }    
    return jsonObject.toString();
  }

  private void buildRelativeStrikes(List<Double> relativeStrikes, JSONObject jsonObject) throws JSONException {
    if (!relativeStrikes.isEmpty()) {
      JSONArray jsonArray = new JSONArray();
      for (Double relativeStrike : relativeStrikes) {
        if (relativeStrike != null) {
          jsonArray.put(relativeStrike);
        }
      }
      jsonObject.put(RELATIVE_STRIKES_FIELD, jsonArray);
    }
  }

  @Override
  public String getTemplate() {
    return TEMPLATE;
  }

}
