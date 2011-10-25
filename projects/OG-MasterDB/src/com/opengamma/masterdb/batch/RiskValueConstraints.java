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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Hibernate bean.
 */
public class RiskValueConstraints {

  private int _id;

  private String _syntheticConstraints;

  public RiskValueConstraints() {
  }

  public RiskValueConstraints(ValueProperties constraints) {
    setSyntheticConstraints(synthesize(constraints));
  }

  private static String escape(Pattern p, String s) {
    return p.matcher(s).replaceAll("\\\\$0");
  }

  private static <T extends Comparable> List<T> sort(Collection<T> c) {
    List<T> list = new ArrayList<T>(c);
    Collections.sort(list);
    return list;
  }

  public static String synthesize(ValueProperties constraints) {
    try {
      Pattern escapePattern = Pattern.compile("[=\\?\\[\\],\\\\]");
      JSONObject json = new JSONObject();

      if (ValueProperties.InfinitePropertiesImpl.class.isInstance(constraints)) {
        json.put("infinity", true).toString();
      } else if (ValueProperties.NearlyInfinitePropertiesImpl.class.isInstance(constraints)) {
        ValueProperties.NearlyInfinitePropertiesImpl nearlyInifite = (ValueProperties.NearlyInfinitePropertiesImpl) constraints;
        JSONArray without = new JSONArray();
        for (String value : sort(nearlyInifite.getWithout())) {
          without.put(escape(escapePattern, value));
        }
        json.put("without", without);
      } else {
        JSONArray properties = new JSONArray();
        if (constraints.getProperties() != null){
          for (String property : sort(constraints.getProperties())) {
            JSONObject propertyJson = new JSONObject();

            propertyJson.put("name", property);
            if (constraints.isOptional(property)) {
              propertyJson.put("optional", true);
            }

            JSONArray values = new JSONArray();
            for (String value : sort(constraints.getValues(property))) {
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

  public ValueProperties toConstraints() {
    try {
      JSONObject jsonObject = new JSONObject(_syntheticConstraints);
      if (jsonObject.has("infinity") && jsonObject.getBoolean("infinity")) {
        return ValueProperties.all();
      } else if (jsonObject.has("without")) {
        JSONArray withoutProperties = jsonObject.getJSONArray("without");
        ValueProperties constraints = ValueProperties.all();
        for (int i = 0; i < withoutProperties.length(); i++) {
          String without = (String) withoutProperties.get(i);
          constraints = constraints.withoutAny(without);
        }
        return constraints;
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
            String value = (String) values.get(i);
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

  public String getSyntheticConstraints() {
    return _syntheticConstraints;
  }

  public void setSyntheticConstraints(String syntheticConstraints) {
    this._syntheticConstraints = syntheticConstraints;
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
    return _syntheticConstraints;
  }

}
