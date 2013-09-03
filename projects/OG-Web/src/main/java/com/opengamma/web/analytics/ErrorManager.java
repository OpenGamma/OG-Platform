/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO this contains the errors for a single view, how does the REST code get hold of it?
 * method on AnalyticsView? not nice. getError and deleteError methods on AnalyticsView? that fits the current model best
 */
/* package */ class ErrorManager {

  private final ErrorIdFactory _idFactory;
  private final ConcurrentMap<String, ErrorInfo> _errors = Maps.newConcurrentMap();

  /* package */ ErrorManager(ErrorIdFactory idFactory) {
    ArgumentChecker.notNull(idFactory, "idFactory");
    _idFactory = idFactory;
  }

  /* package */ String add(Exception exception) {
    String id = _idFactory.generateId();
    _errors.put(id, new ErrorInfo(exception));
    return id;
  }

  /* package */ ErrorInfo get(String id) {
    return _errors.get(id);
  }

  /* package */ void delete(String id) {
    _errors.remove(id);
  }
}
