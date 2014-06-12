/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Map of config values, handling restricted values such as passwords.
 * <p>
 * This class maintains a map of key-value pairs from configuration.
 * Restricted values, such as passwords, are traced to avoid logging sensitive data.
 * <p>
 * This class is mutable.
 */
public final class ConfigProperties {

  /**
   * Text used for hidden configuration.
   */
  public static final String HIDDEN = "*** HIDDEN ***";
  /**
   * The key-value pairs.
   */
  private final LinkedHashMap<String, ConfigProperty> _properties = new LinkedHashMap<>();

  //-------------------------------------------------------------------------
  /**
   * Gets the size of the map.
   * 
   * @return the number of configured properties
   */
  public int size() {
    return _properties.size();
  }

  /**
   * Checks if the key is present.
   * 
   * @param key  the key to find, null returns false
   * @return true if the key is present
   */
  public boolean containsKey(String key) {
    return key != null && _properties.containsKey(key);
  }

  /**
   * Gets the property for the specified key.
   * 
   * @param key  the key to find, not null
   * @return the property, null if not found
   */
  public ConfigProperty get(String key) {
    ArgumentChecker.notNull(key, "key");
    return _properties.get(key);
  }

  /**
   * Gets the value for the specified key.
   * 
   * @param key  the key to find, not null
   * @return the value, null if not found
   */
  public String getValue(String key) {
    ConfigProperty cp = get(key);
    return (cp != null ? cp.getValue() : null);
  }

  /**
   * Gets the set of keys.
   * 
   * @return the set of keys, not null
   */
  public Set<String> keySet() {
    return _properties.keySet();
  }

  /**
   * Gets the set of config properties.
   * 
   * @return the set of properties, not null
   */
  public Set<ConfigProperty> values() {
    return new HashSet<>(_properties.values());
  }

  /**
   * Gets the map of key-value pairs.
   * 
   * @return a copy of the internal map, not null
   */
  public LinkedHashMap<String, String> toMap() {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    for (ConfigProperty cp : _properties.values()) {
      map.put(cp.getKey(), cp.getValue());
    }
    return map;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a configured key-value pair replacing any existing value.
   * <p>
   * This is a standard {@code Map.put(key,value)} operation.
   * Most properties should be resolved and added using
   * {@link #resolveProperty} and {@link #addIfAbsent}.
   * 
   * @param key  the key to add, not null
   * @param value  the value, not null
   */
  public void put(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _properties.put(key, ConfigProperty.of(key, value, false));
  }

  /**
   * Adds all the specified key-value pairs replacing any existing value.
   * <p>
   * This is a standard {@code Map.putAll(Map)} operation.
   * Most properties should be resolved and added using
   * {@link #resolveProperty} and {@link #addIfAbsent}.
   * 
   * @param map  the map to add, not null
   */
  public void putAll(Map<String, String> map) {
    ArgumentChecker.notNull(map, "map");
    for (Entry<String, String> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a resolved property to the map replacing any existing value.
   * <p>
   * Use of this method will propagate hiding of sensitive data.
   * 
   * @param resolved  the resolved property, not null
   */
  public void add(ConfigProperty resolved) {
    ArgumentChecker.notNull(resolved, "resolved");
    _properties.put(resolved.getKey(), resolved);
  }

  /**
   * Adds a resolved property to the map if not currently present.
   * <p>
   * Use of this method will propagate hiding of sensitive data.
   * 
   * @param resolved  the resolved property, not null
   */
  public void addIfAbsent(ConfigProperty resolved) {
    ArgumentChecker.notNull(resolved, "resolved");
    if (_properties.containsKey(resolved.getKey()) == false) {
      add(resolved);
    }
  }

  /**
   * Resolves any ${property} references in the value.
   * <p>
   * This returns a property object that encapsulates the key, value and whether
   * the property contains sensitive information.
   * 
   * @param key  the key, not null
   * @param value  the value to resolve, not null
   * @param lineNum  the line number, for error messages
   * @return the resolved value, not null
   * @throws ComponentConfigException if a variable expansion is not found
   */
  public ConfigProperty resolveProperty(String key, String value, int lineNum) {
    boolean hidden = (key.contains("password") || key.startsWith("shiro."));
    String variable = findVariable(value);
    while (variable != null) {
      ConfigProperty variableProperty = _properties.get(variable);
      if (variableProperty == null) {
        throw new ComponentConfigException("Variable expansion not found: ${" + variable + "}, line " + lineNum);
      }
      hidden = hidden | variableProperty.isHidden();
      value = StringUtils.replaceOnce(value, "${" + variable + "}", variableProperty.getValue());
      variable = findVariable(value);
    }
    return ConfigProperty.of(key, value, hidden);
  }

  /**
   * Finds a variable to replace.
   * 
   * @param value  the value to search, not null
   * @return the variable, null if not found
   */
  private String findVariable(String value) {
    int start = value.lastIndexOf("${");
    if (start >= 0) {
      start += 2;
      int end = value.indexOf("}", start);
      if (end >= 0) {
        return value.substring(start, end);
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts these properties to a loggable map.
   * <p>
   * Sensitive data will be hidden.
   * 
   * @return the loggable map, not null
   */
  public Map<String, String> loggableMap() {
    TreeMap<String, String> map = new TreeMap<String, String>();
    for (ConfigProperty cp : _properties.values()) {
      map.put(cp.getKey(), cp.loggableValue());
    }
    return map;
  }

  /**
   * Gets the loggable value for the specified key.
   * 
   * @param key  the key to find, not null
   * @return the loggable value, null if not found
   */
  public String loggableValue(String key) {
    ArgumentChecker.notNull(key, "key");
    ConfigProperty cp = _properties.get(key);
    return (cp != null ? cp.loggableValue() : null);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return loggableMap().toString();
  }

}
