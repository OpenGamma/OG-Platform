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
 * TODO rename TreeGridStructure? extract superclass with columnGroups and count? or not worth the complication?
 * this is only applicable to portfolio and dep graph grids. primitives is flat and doesn't need the root node
 */
public interface GridBounds {

  int getRowCount();

  int getColumnCount();
}
