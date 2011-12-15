/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Loads configuration from the props/ini format file.
 */
public class ComponentConfigLoader {

  /**
   * Starts the components defined in the specified resource.
   * 
   * @param resource  the config resource to load, not null
   * @return the config, not null
   */
  public ComponentConfig load(Resource resource) {
    List<String> lines = readLines(resource);
    Map<String, String> properties = new HashMap<String, String>();
    ComponentConfig config = new ComponentConfig();
    String group = "global";
    int lineNum = 0;
    for (String line : lines) {
      lineNum++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#") || line.startsWith(";")) {
        continue;
      }
      if (line.startsWith("$") && line.substring(2).contains("=")) {
        parseProperty(line, properties);
        
      } else {
        line = applyProperties(line, properties);
        
        if (line.startsWith("[") && line.endsWith("]")) {
          group = line.substring(1, line.length() - 1);
        
        } else {
          String key = StringUtils.substringBefore(line, "=").trim();
          String value = StringUtils.substringAfter(line, "=").trim();
          if (key.length() == 0) {
            throw new IllegalArgumentException("Invalid key, line " + lineNum);
          }
          config.add(group, key, value);
        }
      }
    }
    return config;
  }

  private void parseProperty(String line, Map<String, String> properties) {
    String key = StringUtils.substringBefore(line, "=").trim();
    String value = StringUtils.substringAfter(line, "=").trim();
    properties.put(key, value);
  }

  private String applyProperties(String line, Map<String, String> properties) {
    for (Entry<String, String> entry : properties.entrySet()) {
      line = StringUtils.replace(line, entry.getKey(), entry.getValue());
    }
    return line;
  }

  /**
   * Reads lines from the resource.
   * 
   * @param resource  the resource to read, not null
   * @return the lines, not null
   */
  private List<String> readLines(Resource resource) {
    try {
      return IOUtils.readLines(resource.getInputStream(), "UTF8");
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to read resource: " + resource, ex);
    }
  }

}
