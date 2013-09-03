/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.util.ArgumentChecker;

/**
 * Creates IDs that are published to the client when errors occur. This is necessary because the IDs in the system are
 * actually the URLs of the objects' REST resources. But the internals of the system don't know about REST
 * (or even HTTP) so the IDs are generated in the REST resource classes and passed in to the core of the system.
 * <p>
 * Unfortunately that won't work for errors that originate in the engine because they don't pass through the REST
 * layer so URLs can't be generated. Which is where this class comes in. The ID template is generated in the REST
 * code and this object is passed into the core so it can generate REST URLs for errors when it needs to.
 */
public class ErrorIdFactory {

  /** Placeholder string that will be replaced in the template with the real error ID. */
  public static final String ERROR_ID = "$errorId$";

  private final String _idTemplate;
  private final AtomicLong _atomicLong = new AtomicLong(0);

  /**
   * @param idTemplate String that is the basis for the generated IDs. Must contain {@link #ERROR_ID} which will be
   * replaced with a unique ID number for each error.
   */
  public ErrorIdFactory(String idTemplate) {
    ArgumentChecker.notEmpty(idTemplate, "idTemplate");
    if (!idTemplate.contains(ERROR_ID)) {
      throw new IllegalArgumentException("idTemplate must contain '" + ERROR_ID + "' but is " + idTemplate);
    }
    _idTemplate = idTemplate;
  }

  /**
   * @return A unique ID based on {@link #ERROR_ID}.
   */
  public String generateId() {
    Long nextId = _atomicLong.getAndIncrement();
    return _idTemplate.replace(ERROR_ID, nextId.toString());
  }
}
