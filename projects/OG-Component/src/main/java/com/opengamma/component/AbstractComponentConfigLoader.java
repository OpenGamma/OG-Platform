/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
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
  private final ConfigProperties _properties;

  /**
   * Creates an instance.
   * 
   * @param logger  the logger, not null
   * @param properties  the properties in use, not null
   */
  public AbstractComponentConfigLoader(ComponentLogger logger, ConfigProperties properties) {
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
  public ConfigProperties getProperties() {
    return _properties;
  }

  //-------------------------------------------------------------------------
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
