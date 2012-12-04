/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Wraps a JSON object so it can be used to build a Joda bean.
 */
/* package */ class JsonBeanDataSource implements BeanDataSource {

  private final JSONObject _json;

  /* package */ JsonBeanDataSource(JSONObject json) {
    ArgumentChecker.notNull(json, "json");
    _json = json;
  }

  /**
   * The JSON library has a NULL sentinel but doesn't document where it's used and where null is used.
   * @return true if value is null or equals JSONObject.NULL
   */
  private static boolean isNull(Object value) {
    return value == null || value == JSONObject.NULL;
  }

  @Override
  public String getValue(String propertyName) {
    Object value = _json.opt(propertyName);
    if (isNull(value)) {
      return null;
    }
    return (String) value;
  }

  @Override
  public List<String> getCollectionValues(String propertyName) {
    JSONArray array = _json.optJSONArray(propertyName);
    if (isNull(array)) {
      return null;
    }
    List<String> strings = Lists.newArrayListWithCapacity(array.length());
    for (int i = 0; i < array.length(); i++) {
      strings.add((String) array.opt(i));
    }
    return strings;
  }

  @Override
  public Map<String, String> getMapValues(String propertyName) {
    JSONObject jsonObject = _json.optJSONObject(propertyName);
    if (isNull(jsonObject)) {
      return null;
    }
    Map<String, String> map = Maps.newHashMap();
    for (Iterator it = jsonObject.keys(); it.hasNext(); ) {
      String key = (String) it.next();
      map.put(key, (String) jsonObject.opt(key));
    }
    return map;
  }

  @Override
  public BeanDataSource getBeanData(String propertyName) {
    JSONObject json = _json.optJSONObject(propertyName);
    if (isNull(json)) {
      return null;
    }
    return new JsonBeanDataSource(json);
  }

  @Override
  public String getBeanTypeName() {
    // TODO should this be configurable?
    try {
      return _json.getString("type");
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to read JSON", e);
    }
  }
}
