/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public interface Formatter<T> {

  String formatForDisplay(T value, ValueSpecification valueSpec);

  Object formatForExpandedDisplay(T value, ValueSpecification valueSpec);

  Object formatForHistory(T value, ValueSpecification valueSpec);

  String getName();

}
