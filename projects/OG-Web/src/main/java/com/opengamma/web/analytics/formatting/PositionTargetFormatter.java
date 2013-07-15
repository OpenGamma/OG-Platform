/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.PositionTarget;

/* package */ class PositionTargetFormatter extends AbstractFormatter<PositionTarget> {

  /** JSON key */
  private static final String NAME = "name";
  /** JSON key */
  private static final String NODE_ID = "nodeId";
  /** JSON key */
  private static final String POSITION_ID = "positionId";

  /* package */  PositionTargetFormatter() {
    super(PositionTarget.class);
  }

  @Override
  public Map<String, Object> formatCell(PositionTarget target, ValueSpecification valueSpec, Object inlineKey) {
    return ImmutableMap.<String, Object>of(NAME, target.getName(),
                                           NODE_ID, target.getNodeId(),
                                           POSITION_ID, target.getPositionId());
  }

  @Override
  public DataType getDataType() {
    return DataType.POSITION;
  }
}
