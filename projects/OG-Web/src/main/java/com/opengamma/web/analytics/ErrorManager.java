/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.rest.ErrorIdFactory;

/**
 * Contains details of the errors that have occurred in the server for a view.
 */
/* package */ class ErrorManager {

  private static final Logger s_logger = LoggerFactory.getLogger(ErrorManager.class);

  private final ErrorIdFactory _idFactory;
  private final ConcurrentMap<String, ErrorInfo> _errors = Maps.newConcurrentMap();

  /* package */ ErrorManager(ErrorIdFactory idFactory) {
    ArgumentChecker.notNull(idFactory, "idFactory");
    _idFactory = idFactory;
  }

  /**
   * Adds information about a new error.
   * @param exception The exception that triggered the error
   * @return The ID of the error
   */
  /* package */ String add(Exception exception) {
    ArgumentChecker.notNull(exception, "exception");
    String id = _idFactory.generateId();
    _errors.put(id, new ErrorInfo(exception));
    s_logger.info("Added error with ID {}, exception {}", id, exception.getMessage());
    return id;
  }

  /**
   * Returns information about an error.
   * @param id The ID of the error
   * @return The error details
   * @throws DataNotFoundException If the ID is unknown
   */
  /* package */ ErrorInfo get(String id) {
    ErrorInfo error = _errors.get(id);
    if (error == null) {
      throw new DataNotFoundException("No error found with ID " + id);
    }
    s_logger.info("Returning error with ID {}, {}", id, error);
    return error;
  }

  /**
   * Deletes an error's details when the client is no longer interested in it.
   * @param id The ID of the error
   * @throws DataNotFoundException If the ID is unknown
   */
  /* package */ void delete(String id) {
    ErrorInfo error = _errors.remove(id);
    if (error == null) {
      throw new DataNotFoundException("No error found with ID " + id);
    }
  }
}
