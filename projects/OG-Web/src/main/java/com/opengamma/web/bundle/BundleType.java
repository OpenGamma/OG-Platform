/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import org.apache.commons.lang.StringUtils;

/**
 * The type of a bundle or fragment, either CSS or Javascript.
 */
public enum BundleType {

  /**
   * The Javascript type.
   */
  JS("js"),
  /**
   * The CSS type.
   */
  CSS("css");

  /**
   * The file suffix.
   */
  private final String _suffix;

  /**
   * Creates an instance.
   * 
   * @param suffix  the file suffix, not null
   */
  BundleType(String suffix) {
    _suffix = suffix;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the file suffix used by the type of file.
   * 
   * @return the file suffix, not null
   */
  public String getSuffix() {
    return _suffix;
  }

  /**
   * Lookup the type using the file suffix.
   * 
   * @param fileName  the file name including the suffix, null returns null
   * @return the bundle type, null if unable to determine
   */
  public static BundleType getType(final String fileName) {    
    if (StringUtils.isNotBlank(fileName)) {
      if (fileName.toLowerCase().endsWith("." + JS.getSuffix())) {
        return JS;
      } else if (fileName.toLowerCase().endsWith("." + CSS.getSuffix())) {
        return CSS;
      }
    }
    return null;
  }

}
