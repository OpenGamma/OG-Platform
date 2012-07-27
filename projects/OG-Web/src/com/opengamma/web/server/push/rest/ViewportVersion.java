/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

/**
 *
 */
public class ViewportVersion {

  private final long _version;

  /* package */ ViewportVersion(long version) {
    _version = version;
  }

  public long getVersion() {
    return _version;
  }
}
