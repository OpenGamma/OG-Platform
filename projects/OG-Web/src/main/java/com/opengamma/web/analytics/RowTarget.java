/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.id.UniqueId;

/**
 * Contains the human-readable name and {@link UniqueId} of the target of a row in the analytics grid.
 */
/* package */  abstract class RowTarget {

  /** The row name. */
  private final String _name;
  /** ID of the row's target. */
  private final UniqueId _nodeId;

  /* package */  RowTarget(String name, UniqueId nodeId) {
    _name = name;
    _nodeId = nodeId;
  }

  /**
   * @return The name of the row's target
   */
  public String getName() {
    return _name;
  }

  /**
   * @return The ID of the row's target
   */
  public UniqueId getNodeId() {
    return _nodeId;
  }
}
