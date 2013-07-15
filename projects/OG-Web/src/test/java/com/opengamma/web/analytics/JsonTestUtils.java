package com.opengamma.web.analytics;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Objects;

/**
 * Helper methods for comparing JSON.
 */
public class JsonTestUtils {

  private JsonTestUtils() {
  }

  /**
   * Checks all elements of two JSON arrays for equality. Individual elements are checked using
   * {@link #equal(Object, Object)}.
   * @param array1 A JSON array
   * @param array2 Another JSON array
   * @return {@code true} if the arrays are the same size and every corresponding element is equal
   * @throws JSONException Never
   */
  public static boolean equal(JSONArray array1, JSONArray array2) throws JSONException {
    if (array1.length() != array2.length()) {
      return false;
    }
    for (int i = 0; i < array1.length(); i++) {
      Object value1 = array1.get(i);
      Object value2 = array2.get(i);
      if (!equal(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks all values of two JSON objects for equality. Individual elements are checked using
   * {@link #equal(Object, Object)}.
   * @param object1 A JSON object
   * @param object2 A JSON object
   * @return {@code true} if the objects contains the same mappings and every corresponding value is equal.
   * @throws JSONException Never
   */
  public static boolean equal(JSONObject object1, JSONObject object2) throws JSONException {
    if (object1.length() != object2.length()) {
      return false;
    }
    for (Iterator<?> it = object1.keys(); it.hasNext(); ) {
      String key = (String) it.next();
      Object value1 = object1.get(key);
      Object value2 = object2.get(key);
      if (!equal(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks two objects for equality. If they are instances of {@link JSONObject} or {@link JSONArray} they are
   * checked recursively using {@link #equal(JSONObject, JSONObject)} and {@link #equal(JSONArray, JSONArray)}.
   * Values of other types are checked using {@link Objects#equal(Object, Object)}.
   * @param value1
   * @param value2
   * @return
   * @throws JSONException
   */
  public static boolean equal(Object value1, Object value2) throws JSONException {
    if (value1 instanceof JSONArray && value2 instanceof JSONArray) {
      return equal((JSONArray) value1, (JSONArray) value2);
    } else if (value1 instanceof JSONObject && value2 instanceof JSONObject) {
      return equal((JSONObject) value1, (JSONObject) value2);
    }
    return Objects.equal(value1, value2);
  }
}
