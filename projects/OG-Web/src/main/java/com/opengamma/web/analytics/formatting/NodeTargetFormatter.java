/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.NodeTarget;

/* package */ class NodeTargetFormatter extends AbstractFormatter<NodeTarget> {

  /** JSON key */
  private static final String NAME = "name";
  /** JSON key */
  private static final String NODE_ID = "nodeId";

  /* package */  NodeTargetFormatter() {
    super(NodeTarget.class);
  }

  @Override
  public Object formatCell(NodeTarget target, ValueSpecification valueSpec) {
    return ImmutableMap.of(NAME, target.getName(), NODE_ID, target.getNodeId().getObjectId());
  }

  @Override
  public DataType getDataType() {
    return DataType.NODE;
  }
}
