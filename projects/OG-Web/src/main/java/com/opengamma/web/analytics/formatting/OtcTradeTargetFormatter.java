/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
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
  public Map<String, Object> formatCell(OtcTradeTarget target, ValueSpecification valueSpec, Object inlineKey) {
    Map<String, Object> results = Maps.newHashMap();
    results.put(NAME, target.getName());
    results.put(NODE_ID, target.getNodeId().getObjectId());
    results.put(POSITION_ID, target.getPositionId().getObjectId());
    UniqueId tradeId = target.getTradeId();
    if (tradeId != null) {
      results.put(TRADE_ID, tradeId.getObjectId());
    }
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.OTC_TRADE;
  }
}
