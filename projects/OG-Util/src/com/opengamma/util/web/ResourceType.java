/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.web;

import org.apache.commons.lang.StringUtils;

/**
 * ResourceType representation
 */
public enum ResourceType {
  /**
   * Javascript Type
   */
  JS("js"),
  /**
   * CSS Type
   */
  CSS("css");

  private final String _suffix;

  ResourceType(String suffix) {
    _suffix = suffix;
  }

  public String getSuffix() {
    return _suffix;
  }

  public static ResourceType getType(String fileName) {
    if (StringUtils.isBlank(fileName)) {
      return (fileName.endsWith("." + JS.getSuffix())) ? JS : (fileName.endsWith("." + CSS.getSuffix())) ? CSS : null;
    }
    return null;
  }
}
