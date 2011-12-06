/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.value.ValueProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import static com.opengamma.util.functional.Functional.sort;

/**
 * Hibernate bean.
 */
public class RiskValueProperties {

  private int _id;

  private String _syntheticForm;

  public RiskValueProperties() {
  }

  public RiskValueProperties(ValueProperties requirement) {
    setSyntheticForm(synthesize(requirement));
  }

  private static String escape(Pattern p, String s) {
    return p.matcher(s).replaceAll("\\\\$0");
  }

  public static String synthesize(ValueProperties requirement) {
    try {
      Pattern escapePattern = Pattern.compile("[=\\?\\[\\],\\\\]");
      JSONObject json = new JSONObject();

      if (ValueProperties.InfinitePropertiesImpl.class.isInstance(requirement)) {
        json.put("infinity", true).toString();
      } else if (ValueProperties.NearlyInfinitePropertiesImpl.class.isInstance(requirement)) {
        ValueProperties.NearlyInfinitePropertiesImpl nearlyInifite = (ValueProperties.NearlyInfinitePropertiesImpl) requirement;
        JSONArray without = new JSONArray();
        for (String value : sort(nearlyInifite.getWithout())) {
          without.put(escape(escapePattern, value));
        }
        json.put("without", without);
      } else {
        JSONArray properties = new JSONArray();
        if (requirement.getProperties() != null) {
          for (String property : sort(requirement.getProperties())) {
            JSONObject propertyJson = new JSONObject();

            propertyJson.put("name", property);
            if (requirement.isOptional(property)) {
              propertyJson.put("optional", true);
            }

            JSONArray values = new JSONArray();
            for (String value : sort(requirement.getValues(property))) {
              values.put(escape(escapePattern, value));
            }
            propertyJson.put("values", values);
            properties.put(propertyJson);
          }
        }
        json.put("properties", properties);
      }
      return json.toString();
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  public ValueProperties toProperties() {
    try {
      JSONObject jsonObject = new JSONObject(_syntheticForm);
      if (jsonObject.has("infinity") && jsonObject.getBoolean("infinity")) {
        return ValueProperties.all();
      } else if (jsonObject.has("without")) {
        JSONArray withoutProperties = jsonObject.getJSONArray("without");
        ValueProperties requirement = ValueProperties.all();
        for (int i = 0; i < withoutProperties.length(); i++) {
          String without = (String) withoutProperties.get(i);
          requirement = requirement.withoutAny(without);
        }
        return requirement;
      } else if (jsonObject.has("properties") && jsonObject.getJSONArray("properties") != null) {
        JSONArray withProperties = jsonObject.getJSONArray("properties");
        final ValueProperties.Builder builder = ValueProperties.builder();
        for (int i = 0; i < withProperties.length(); i++) {
          JSONObject property = (JSONObject) withProperties.get(i);
          String name = property.getString("name");
          if (jsonObject.has("optional") && property.getBoolean("optional")) {
            builder.withOptional(name);
          }
          JSONArray values = (JSONArray) property.get("values");
          for (int j = 0; j < values.length(); j++) {
            String value = (String) values.get(j);
            builder.with(name, value);
          }
        }
        return builder.get();
      } else {
        return null;
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  public int getId() {
    return _id;
  }

  public void setId(int id) {
    _id = id;
  }

  public String getSyntheticForm() {
    return _syntheticForm;
  }

  public void setSyntheticForm(String syntheticForm) {
    this._syntheticForm = syntheticForm;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return _syntheticForm;
  }

}
