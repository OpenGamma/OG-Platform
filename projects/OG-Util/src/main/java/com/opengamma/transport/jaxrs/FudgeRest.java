/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import javax.ws.rs.core.MediaType;

/**
 * Overall helper class for RESTful Fudge.
 */
public final class FudgeRest {

  /**
   * Media type for Fudge.
   */
  public static final String MEDIA = "application/vnd.fudgemsg";
  /**
   * Media type for Fudge.
   */
  public static final MediaType MEDIA_TYPE = MediaType.valueOf(MEDIA);

  /**
   * Restricted constructor.
   */
  private FudgeRest() {
  }

}
