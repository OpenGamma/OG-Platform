/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

import com.opengamma.util.ResourceUtils;

/**
 * Loads configuration from the properties format file.
 * <p>
 * The format is line-based as follows:<br>
 *  <code>#</code> or <code>;</code> for comment lines (at the start of the line)<br>
 *  <code>key = value</code> declares a property<br>
 *  <code>MANAGER.INCLUDE = resource</code> declares a resource to be included immediately<br>
 *  <code>${key}</code> is replaced by an earlier replacement declaration<br>
 *  Everything is trimmed as necessary.
 *  <p>
 *  The {@link Properties} class is not used for parsing as it does not preserver order.
 */
public class ComponentConfigPropertiesLoader extends AbstractComponentConfigLoader {

  /**
   * Creates an instance.
   * 
   * @param logger  the logger, not null
   * @param properties  the properties being built up, not null
   */
  public ComponentConfigPropertiesLoader(ComponentLogger logger, ConcurrentMap<String, String> properties) {
    super(logger, properties);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the properties file.
   * 
   * @param resource  the config resource to load, not null
   * @param depth  the depth of the properties file, used for logging
   * @return the next configuration file to load, null if not specified
   * @throws ComponentConfigException if the resource cannot be loaded
   */
  public String load(final Resource resource, final int depth) {
    try {
      return doLoad(resource, depth);
    } catch (RuntimeException ex) {
      throw new ComponentConfigException("Unable to load properties file: " + resource, ex);
    }
  }

  /**
   * Loads the properties file.
   * 
   * @param resource  the config resource to load, not null
   * @param depth  the depth of the properties file, used for logging
   * @return the next configuration file to load, null if not specified
   * @throws ComponentConfigException if the resource cannot be loaded
   */
  private String doLoad(final Resource resource, final int depth) {
    final Map<String, String> fileProperties = new HashMap<String, String>();
    final List<String> lines = readLines(resource);
    int lineNum = 0;
    for (String line : lines) {
      lineNum++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#") || line.startsWith(";")) {
        continue;
      }
      int equalsPosition = line.indexOf('=');
      if (equalsPosition < 0) {
        throw new ComponentConfigException("Invalid format, line " + lineNum);
      }
      String key = line.substring(0, equalsPosition).trim();
      String value = line.substring(equalsPosition + 1).trim();
      if (key.length() == 0) {
        throw new ComponentConfigException("Invalid empty key, line " + lineNum);
      }
      if (fileProperties.containsKey(key)) {
        throw new ComponentConfigException("Invalid file, key '" + key + "' specified twice, line " + lineNum);
      }
      
      // resolve ${} references
      value = resolveProperty(value, lineNum);
      
      // handle includes
      if (key.equals(ComponentManager.MANAGER_INCLUDE)) {
        handleInclude(resource, value, depth);
      } else {
        // store property
        fileProperties.put(key, value);
        if (key.equals(ComponentManager.MANAGER_NEXT_FILE) == false) {
          getProperties().putIfAbsent(key, value);  // first definition wins
        }
      }
    }
    return fileProperties.get(ComponentManager.MANAGER_NEXT_FILE);
  }

  /**
   * Handle the inclusion of another file.
   * 
   * @param baseResource  the base resource, not null
   * @param includeFile  the resource to include, not null
   * @param depth  the depth of the properties file, used for logging
   * @throws ComponentConfigException if the included resource cannot be loaded
   */
  private void handleInclude(final Resource baseResource, String includeFile, final int depth) {
    // find resource
    Resource include;
    try {
      include = ResourceUtils.createResource(includeFile);
    } catch (Exception ex) {
      try {
        include = baseResource.createRelative(includeFile);
      } catch (Exception ex2) {
        throw new ComponentConfigException(ex2.getMessage(), ex2);
      }
    }
    
    // load and merge
    getLogger().logInfo(StringUtils.repeat(" ", depth) + "   Including item: " + ResourceUtils.getLocation(include));
    load(include, depth + 1);
  }

}
