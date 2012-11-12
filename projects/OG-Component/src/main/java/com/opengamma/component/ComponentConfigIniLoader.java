/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Loads configuration from the INI format file.
 * <p>
 * The format is line-based as follows:<br>
 *  <code>#</code> or <code>;</code> for comment lines (at the start of the line)<br>
 *  <code>${key}</code> is replaced by an earlier replacement declaration<br>
 *  <code>[group]</code> defines the start of a named group of configs<br>
 *  <code>key = value</code> defines a single config element within a group<br>
 *  the "global" group is used to add keys to the set of properties used for replacement<br>
 *  Everything is trimmed as necessary.
 */
public class ComponentConfigIniLoader extends AbstractComponentConfigLoader {

  /**
   * The pattern to match [group].key
   */
  private static final Pattern GROUP_OVERRIDE = Pattern.compile("\\[" + "([^\\]]+)" + "\\]" + "[.]" + "(.+)");

  /**
   * Creates an instance.
   * 
   * @param logger  the logger, not null
   * @param properties  the properties in use, not null
   */
  public ComponentConfigIniLoader(ComponentLogger logger, ConcurrentMap<String, String> properties) {
    super(logger, properties);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the configuration defining components from the specified resource.
   * <p>
   * The specified properties are simple key=value pairs and must not be surrounded with ${}.
   * 
   * @param resource  the config resource to load, not null
   * @return the config, not null
   */
  public ComponentConfig load(Resource resource) {
    try {
      return doLoad(resource);
    } catch (RuntimeException ex) {
      throw new OpenGammaRuntimeException("Unable to load INI file: " + resource, ex);
    }
  }

  /**
   * Loads the INI file.
   * 
   * @param resource  the config resource to load, not null
   * @return the config, not null
   */
  protected ComponentConfig doLoad(Resource resource) {
    List<String> lines = readLines(resource);
    ComponentConfig config = new ComponentConfig();
    String group = null;
    int lineNum = 0;
    for (String line : lines) {
      lineNum++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#") || line.startsWith(";")) {
        continue;
      }
      if (line.startsWith("[") && line.endsWith("]")) {
        group = line.substring(1, line.length() - 1);
        
      } else if (group == null) {
        throw new OpenGammaRuntimeException("Invalid format, properties must be specified within a [group], line " + lineNum);
        
      } else {
        int equalsPosition = line.indexOf('=');
        if (equalsPosition < 0) {
          throw new OpenGammaRuntimeException("Invalid format, line " + lineNum);
        }
        String key = line.substring(0, equalsPosition).trim();
        String value = line.substring(equalsPosition + 1).trim();
        if (key.length() == 0) {
          throw new IllegalArgumentException("Invalid empty key, line " + lineNum);
        }
        if (config.contains(group, key)) {
          throw new IllegalArgumentException("Invalid file, key '" + key + "' specified twice, line " + lineNum);
        }
        
        // resolve ${} references
        value = resolveProperty(value, lineNum);
        
        // store group property
        config.put(group, key, value);
        if (group.equals("global")) {
          getProperties().put(key, value);
        }
      }
    }
    
    // override config with properties prefixed by INI
    List<String[]> iniProperties = extractIniOverrideProperties();
    for (String[] array : iniProperties) {
      config.getGroup(array[0]);  // validate group (but returns a copy of the inner map)
      config.put(array[0], array[1], array[2]);
      getLogger().logDebug("  Replacing group property: [" + array[0] + "]." + array[1] + "=" + array[2]);
    }
    return config;
  }

  /**
   * Extracts any properties that match the group name style "[group].key".
   * <p>
   * These directly override any INI file settings.
   * 
   * @return the extracted set of INI properties, not null
   */
  private List<String[]> extractIniOverrideProperties() {
    List<String[]> extracted = new ArrayList<String[]>();
    for (String key : getProperties().keySet()) {
      Matcher matcher = GROUP_OVERRIDE.matcher(key);
      if (matcher.matches()) {
        String group = matcher.group(1);
        String propertyKey = matcher.group(2);
        String[] array = {group, propertyKey, getProperties().get(key)};
        extracted.add(array);
      }
    }
    return extracted;
  }

}
