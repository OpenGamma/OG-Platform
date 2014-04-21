/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

/**
 * Abstract class for loading component configuration.
 */
public abstract class AbstractComponentConfigLoader {

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
   * @param properties  the properties in use, not null
   */
  public AbstractComponentConfigLoader(ComponentLogger logger, ConcurrentMap<String, String> properties) {
    _logger = logger;
    _properties = properties;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the logger.
   * 
   * @return the logger, not null
   */
  public ComponentLogger getLogger() {
    return _logger;
  }

  /**
   * Gets the properties.
   * 
   * @return the properties, not null
   */
  public ConcurrentMap<String, String> getProperties() {
    return _properties;
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves any ${property} references in the value.
   * 
   * @param value  the value to resolve, not null
   * @param lineNum  the line number, for error messages
   * @return the resolved value, not null
   * @throws ComponentConfigException if a variable expansion is not found
   */
  protected String resolveProperty(String value, int lineNum) {
    String variable = findVariable(value);
    while (variable != null) {
      if (_properties.containsKey(variable) == false) {
        throw new ComponentConfigException("Variable expansion not found: ${" + variable + "}, line " + lineNum);
      }
      value = StringUtils.replaceOnce(value, "${" + variable + "}", _properties.get(variable));
      variable = findVariable(value);
    }
    return value;
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

  /**
   * Reads lines from the resource.
   * 
   * @param resource  the resource to read, not null
   * @return the lines, not null
   * @throws ComponentConfigException if the resource cannot be read
   */
  protected List<String> readLines(Resource resource) {
    try {
      return IOUtils.readLines(resource.getInputStream(), "UTF8");
    } catch (IOException ex) {
      throw new ComponentConfigException("Unable to read resource: " + resource, ex);
    }
  }

}
