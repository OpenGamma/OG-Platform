/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
   * The logger.
   */
  private final ComponentLogger _logger;
  /**
   * The set of properties being built up.
   */
  private final ConcurrentMap<String, String> _properties;

  /**
   * Creates an instance.
   * 
   * @param logger  the logger, not null
   * @param properties  the properties being built up, not null
   */
  public ComponentConfigPropertiesLoader(ComponentLogger logger, ConcurrentMap<String, String> properties) {
    _logger = logger;
    _properties = properties;
  }

  //-------------------------------------------------------------------------
  /**
   * Starts the components defined in the specified resource.
   * <p>
   * The specified properties are simple key=value pairs and must not be surrounded with ${}.
   * 
   * @param resource  the config resource to load, not null
   * @param depth  the depth of the properties file, used for logging
   * @return the combined set of properties, not null
   */
  public String load(final Resource resource, final int depth) {
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
        throw new OpenGammaRuntimeException("Invalid format, line " + lineNum);
      }
      String key = line.substring(0, equalsPosition).trim();
      String value = line.substring(equalsPosition + 1).trim();
      if (key.length() == 0) {
        throw new IllegalArgumentException("Invalid empty key, line " + lineNum);
      }
      if (fileProperties.containsKey(key)) {
        throw new IllegalArgumentException("Invalid file, key '" + key + "' specified twice, line " + lineNum);
      }
      
      // resolve ${} references
      value = resolveProperty(value);
      
      // handle includes
      if (key.equals(ComponentManager.MANAGER_INCLUDE)) {
        handleInclude(resource, value, depth);
      } else {
        fileProperties.put(key, value);
        if (key.equals(ComponentManager.MANAGER_NEXT_FILE) == false) {
          _properties.putIfAbsent(key, value);  // first definition wins
        }
      }
    }
    return fileProperties.get(ComponentManager.MANAGER_NEXT_FILE);
  }

  /**
   * Resolves any ${property} references in the value.
   * 
   * @param value  the value to resolve, not null
   * @return the resolved value, not null
   */
  protected String resolveProperty(String value) {
    String variable = StringUtils.substringBetween(value, "${", "}");
    while (variable != null) {
      if (_properties.containsKey(variable) == false) {
        throw new OpenGammaRuntimeException("Variable expansion not found: ${" + variable + "}");
      }
      value = StringUtils.replaceOnce(value, "${" + variable + "}", _properties.get(variable));
      variable = StringUtils.substringBetween(value, "${", "}");
    }
    return value;
  }

  /**
   * Handle the inclusion of another file.
   * 
   * @param baseResource  the base resource, not null
   * @param includeFile  the resource to include, not null
   * @param depth  the depth of the properties file, used for logging
   */
  protected void handleInclude(final Resource baseResource, String includeFile, final int depth) {
    // find resource
    Resource include;
    try {
      include = ComponentManager.createResource(includeFile);
    } catch (Exception ex) {
      try {
        include = baseResource.createRelative(includeFile);
      } catch (Exception ex2) {
        throw new OpenGammaRuntimeException(ex2.getMessage(), ex2);
      }
    }
    
    // load and merge
    _logger.logInfo(StringUtils.repeat(" ", depth) + "   Including file: " + include);
    load(include, depth + 1);
  }

  /**
   * Reads lines from the resource.
   * 
   * @param resource  the resource to read, not null
   * @return the lines, not null
   */
  protected List<String> readLines(Resource resource) {
    try {
      return IOUtils.readLines(resource.getInputStream(), "UTF8");
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("Unable to read resource: " + resource, ex);
    }
  }

}
