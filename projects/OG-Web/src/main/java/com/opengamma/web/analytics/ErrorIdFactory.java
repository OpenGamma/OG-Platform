/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class ErrorIdFactory {

  private static final String ERROR_ID = "{errorId}";

  private final String _idTemplate;
  private final AtomicLong _atomicLong = new AtomicLong(0);

  /* package */ ErrorIdFactory(String idTemplate) {
    ArgumentChecker.notEmpty(idTemplate, "idTemplate");
    if (!idTemplate.contains(ERROR_ID)) {
      throw new IllegalArgumentException("idTemplate must contain '" + ERROR_ID + "' but is " + idTemplate);
    }
    _idTemplate = idTemplate;
  }

  /* package */ String generateId() {
    Long nextId = _atomicLong.getAndIncrement();
    return _idTemplate.replace(ERROR_ID, nextId.toString());
  }
}
