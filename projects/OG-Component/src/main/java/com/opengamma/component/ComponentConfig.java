/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * The component configuration.
 * <p>
 * This is an object representation of the configuration typically read from a file.
 */
public class ComponentConfig {

  /**
   * The config.
   */
  private final LinkedHashMap<String, LinkedHashMap<String, String>> _config = new LinkedHashMap<String, LinkedHashMap<String, String>>();

  /**
   * Creates an instance.
   */
  public ComponentConfig() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the group names.
   * 
   * @return the group names, not null
   */
  public Set<String> getGroups() {
    return Collections.unmodifiableSet(_config.keySet());
  }

  /**
   * Gets a group by name.
   * 
   * @param groupKey  the group key, not null
   * @return a modifiable copy of the configured group, not null
   * @throws ComponentConfigException if the group is not found
   */
  public LinkedHashMap<String, String> getGroup(String groupKey) {
    LinkedHashMap<String, String> config = _config.get(groupKey);
    if (config == null) {
      throw new ComponentConfigException("Config group not found: [" + groupKey + "]");
    }
    return new LinkedHashMap<String, String>(config);
  }

  /**
   * Adds an empty group into the config, throwing an exception if it already exists.
   * 
   * @param groupKey  the group key, not null
   * @throws ComponentConfigException if the group already exists
   */
  public void addGroup(String groupKey) {
    ArgumentChecker.notNull(groupKey, "groupKey");
    if (_config.containsKey(groupKey)) {
      throw new ComponentConfigException("Group cannot be added as it already exists: " + groupKey);
    }
    _config.put(groupKey, new LinkedHashMap<String, String>());
  }

  /**
   * Puts a mapping into the config.
   * <p>
   * This creates the group if necessary, and replaces any existing key.
   * 
   * @param groupKey  the group key, not null
   * @param innerKey  the inner key, not null
   * @param value  the value, not null
   */
  public void put(String groupKey, String innerKey, String value) {
    ArgumentChecker.notNull(groupKey, "groupKey");
    ArgumentChecker.notNull(innerKey, "innerKey");
    ArgumentChecker.notNull(value, "value");
    LinkedHashMap<String, String> config = _config.get(groupKey);
    if (config == null) {
      config = new LinkedHashMap<String, String>();
      _config.put(groupKey, config);
    }
    config.put(innerKey, value);
  }

  /**
   * Checks if the config contains the specified key.
   * 
   * @param groupKey  the group key, not null
   * @param innerKey  the inner key, not null
   * @return whether the config contains the key
   */
  public boolean contains(String groupKey, String innerKey) {
    ArgumentChecker.notNull(groupKey, "groupKey");
    ArgumentChecker.notNull(innerKey, "innerKey");
    LinkedHashMap<String, String> config = _config.get(groupKey);
    return (config != null && config.containsKey(innerKey));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Config" + new ArrayList<String>(_config.keySet());
  }

}
