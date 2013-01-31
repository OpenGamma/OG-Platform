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

  private static final String NAME = "name";
  private static final String ID = "id";
  private static final String POSITION_ID = "positionId";

  /* package */ OtcTradeTargetFormatter() {
    super(OtcTradeTarget.class);
  }

  @Override
  public Object formatCell(OtcTradeTarget trade, ValueSpecification valueSpec) {
    return ImmutableMap.of(NAME, trade.getName(),
                           ID, trade.getId().getObjectId(),
                           POSITION_ID, trade.getPositionId().getObjectId());
  }

  @Override
  public DataType getDataType() {
    return DataType.OTC_TRADE;
  }
}
