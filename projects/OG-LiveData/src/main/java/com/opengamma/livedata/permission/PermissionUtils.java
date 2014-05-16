/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.permission;

/**
 * Utility class for working with permissions in OG-liveData
 */
public final class PermissionUtils {

  /**
   * Name of the permission field in live or snapshot data.
   * The value is expected to be a permission string.
   */
  public static final String LIVE_DATA_PERMISSION_FIELD = "og:livedata:permission";
  /**
   * Name of the field used to indicate a permission error.
   * The value is expected to be a string error message.
   */
  public static final String LIVE_DATA_PERMISSION_DENIED_FIELD = "og:livedata:permission:denied";

  /**
   * Restricted constructor.
   */
  private PermissionUtils() {
  }
}
