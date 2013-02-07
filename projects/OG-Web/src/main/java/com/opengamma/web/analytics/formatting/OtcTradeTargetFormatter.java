/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.OtcTradeTarget;

/**
 * Formats {@link OtcTradeTarget}s for sending to the client as JSON.
 */
/* package */ class OtcTradeTargetFormatter extends AbstractFormatter<OtcTradeTarget> {

  /** JSON key */
  private static final String NAME = "name";
  /** JSON key */
  private static final String NODE_ID = "nodeId";
  /** JSON key */
  private static final String POSITION_ID = "positionId";
  /** JSON key */
  private static final String TRADE_ID = "tradeId";

  /* package */ OtcTradeTargetFormatter() {
    super(OtcTradeTarget.class);
  }

  @Override
  public Object formatCell(OtcTradeTarget target, ValueSpecification valueSpec) {
    return ImmutableMap.of(NAME, target.getName(),
                           NODE_ID, target.getNodeId().getObjectId(),
                           POSITION_ID, target.getPositionId().getObjectId(),
                           TRADE_ID, target.getTradeId().getObjectId());
  }

  @Override
  public DataType getDataType() {
    return DataType.OTC_TRADE;
  }
}
