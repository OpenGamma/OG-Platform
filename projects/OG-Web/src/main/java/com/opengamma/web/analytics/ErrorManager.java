/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains details of the errors that have occurred in the server for a view.
 */
/* package */ class ErrorManager {

  private static final Logger s_logger = LoggerFactory.getLogger(ErrorManager.class);

  private final ConcurrentMap<Long, ErrorInfo> _errors = Maps.newConcurrentMap();
  private final AtomicLong _nextId = new AtomicLong(0);
  private final String _errorId;

  /* package */ ErrorManager(String errorId) {
    _errorId = errorId;
  }

  /**
   * Adds information about a new error.
   * @param throwable The throwable that triggered the error
   * @return The ID of the error
   */
  /* package */ String add(Throwable throwable) {
    ArgumentChecker.notNull(throwable, "throwable");
    long id = _nextId.getAndIncrement();
    _errors.put(id, new ErrorInfo(id, throwable));
    s_logger.info("Added error with ID {}, throwable {}", id, throwable.getMessage());
    return _errorId;
  }

  /**
   * Returns information about an error.
   * @return The error details
   * @throws DataNotFoundException If the ID is unknown
   */
  /* package */ List<ErrorInfo> get() {
    List<ErrorInfo> errors = Lists.newArrayList(_errors.values());
    s_logger.info("Returning errors {}", errors);
    return errors;
  }

  /**
   * Deletes an error's details when the client is no longer interested in it.
   * @param id The ID of the error
   * @throws DataNotFoundException If the ID is unknown
   */
  /* package */ void delete(long id) {
    ErrorInfo error = _errors.remove(id);
    if (error == null) {
      throw new DataNotFoundException("No error found with ID " + id);
    }
  }
}
