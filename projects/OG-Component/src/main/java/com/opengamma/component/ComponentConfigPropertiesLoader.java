/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Loads configuration from the properties format file.
 * <p>
 * The format is line-based as follows:<br>
 *  <code>#</code> or <code>;</code> for comment lines (at the start of the line)<br>
 *  </code>key = value</code> declares a property<br>
 *  </code>${key}</code> is replaced by an earlier replacement declaration<br>
 *  Everything is trimmed as necessary.
 *  <p>
 *  The {@link Properties} class is not used for parsing as it does not preserver order.
 */
public class ComponentConfigPropertiesLoader {

  /**
   * Starts the components defined in the specified resource.
   * <p>
   * The specified properties are simple key=value pairs and must not be surrounded with ${}.
   * 
   * @param resource  the config resource to load, not null
   * @return the combined set of properties, not null
   */
  public Map<String, String> load(Resource resource) {
    List<String> lines = readLines(resource);
    Map<String, String> properties = new LinkedHashMap<String, String>();
    int lineNum = 0;
    for (String line : lines) {
      lineNum++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#") || line.startsWith(";")) {
        continue;
      }
      int equalsPosition = line.indexOf('=');
      if (equalsPosition < 0) {
        throw new OpenGammaRuntimeException("Invalid format, line " + lineNum);
      }
      String key = line.substring(0, equalsPosition).trim();
      String value = line.substring(equalsPosition + 1).trim();
      if (key.length() == 0) {
        throw new IllegalArgumentException("Invalid empty key, line " + lineNum);
      }
      if (properties.containsKey(key)) {
        throw new IllegalArgumentException("Invalid file, key '" + key + "' specified twice, line " + lineNum);
      }
      properties.put(key, value);
    }
    return properties;
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
