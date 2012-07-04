/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public interface GridBounds {

  int getRowCount();

  int getColumnCount();
}
