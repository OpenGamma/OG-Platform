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

  /** The underlying data. */
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
  public Object getValue(String propertyName) {
    return createValue(_json.opt(propertyName));
  }

  @Override
  public List<Object> getCollectionValues(String propertyName) {
    JSONArray array = _json.optJSONArray(propertyName);
    return createCollection(array);
  }

  // TODO this won't cope with maps with beans as keys or values. need to fix
  @Override
  public Map<?, ?> getMapValues(String propertyName) {
    JSONObject jsonObject = _json.optJSONObject(propertyName);
    if (isNull(jsonObject)) {
      return null;
    }
    Map<String, Object> map = Maps.newHashMap();
    for (Iterator<?> it = jsonObject.keys(); it.hasNext(); ) {
      String key = (String) it.next();
      map.put(key, jsonObject.opt(key));
    }
    return map;
  }

  private static Object createValue(Object object) {
    if (isNull(object)) {
      return null;
    } else if (object instanceof JSONObject) {
      return new JsonBeanDataSource((JSONObject) object);
    } else if (object instanceof JSONArray) {
      return createCollection((JSONArray) object);
    } else {
      return object;
    }
  }

  private static List<Object> createCollection(JSONArray array) {
    if (isNull(array)) {
      return null;
    }
    List<Object> items = Lists.newArrayList();
    for (int i = 0; i < array.length(); i++) {
      Object item;
      try {
        item = array.get(i);
      } catch (JSONException e) {
        return null;
      }
      items.add(createValue(item));
    }
    return items;
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
