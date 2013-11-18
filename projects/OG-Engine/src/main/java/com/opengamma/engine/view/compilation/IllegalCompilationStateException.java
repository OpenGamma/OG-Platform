/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import com.opengamma.id.ObjectId;

/**
 * Signals that the state assumed for incremental compilation is invalid and the caller must reconsider the request.
 */
public class IllegalCompilationStateException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final ObjectId _invalidIdentifier;

  public IllegalCompilationStateException(final ObjectId invalidId) {
    _invalidIdentifier = invalidId;
  }

  @Override
  public String getMessage() {
    return "Illegal state " + _invalidIdentifier;
  }

  public ObjectId getInvalidIdentifier() {
    return _invalidIdentifier;
  }

}
