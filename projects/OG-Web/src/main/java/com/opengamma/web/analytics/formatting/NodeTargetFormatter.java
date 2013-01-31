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

  private static final String NAME = "name";
  private static final String ID = "id";

  /* package */  NodeTargetFormatter() {
    super(NodeTarget.class);
  }

  @Override
  public Object formatCell(NodeTarget value, ValueSpecification valueSpec) {
    return ImmutableMap.of(NAME, value.getName(), ID, value.getId().getObjectId());
  }

  @Override
  public DataType getDataType() {
    return DataType.NODE;
  }
}
