/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.FungibleTradeTarget;

/* package */ class FungibleTradeTargetFormatter extends AbstractFormatter<FungibleTradeTarget> {

  /** JSON key */
  private static final String NAME = "name";
  /** JSON key */
  private static final String NODE_ID = "nodeId";
  /** JSON key */
  private static final String POSITION_ID = "positionId";
  /** JSON key */
  private static final String TRADE_ID = "tradeId";

  /* package */  FungibleTradeTargetFormatter() {
    super(FungibleTradeTarget.class);
  }

  @Override
  public Map<String, Object> formatCell(FungibleTradeTarget target, ValueSpecification valueSpec, Object inlineKey) {
    return ImmutableMap.<String, Object>of(NAME, target.getName(),
                                           NODE_ID, target.getNodeId().getObjectId(),
                                           POSITION_ID, target.getPositionId().getObjectId(),
                                           TRADE_ID, target.getTradeId().getObjectId());
  }

  @Override
  public DataType getDataType() {
    return DataType.FUNGIBLE_TRADE;
  }
}
