/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.id.UniqueId;

/**
 * {@link RowTarget} implementation for rows displaying data for a portfolio node.
 */
public class NodeTarget extends RowTarget {

  /* package */ NodeTarget(String name, UniqueId nodeId) {
    super(name, nodeId);
  }
}
