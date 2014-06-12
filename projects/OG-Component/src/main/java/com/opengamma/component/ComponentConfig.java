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
  private final LinkedHashMap<String, ConfigProperties> _config = new LinkedHashMap<>();

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
   * @return the modifiable configured group, not null
   * @throws ComponentConfigException if the group is not found
   */
  public ConfigProperties getGroup(String groupKey) {
    ConfigProperties config = _config.get(groupKey);
    if (config == null) {
      throw new ComponentConfigException("Config group not found: [" + groupKey + "]");
    }
    return config;
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
    _config.put(groupKey, new ConfigProperties());
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
    ConfigProperties config = _config.get(groupKey);
    return (config != null && config.containsKey(innerKey));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Config" + new ArrayList<String>(_config.keySet());
  }

}
