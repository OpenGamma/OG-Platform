/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.FungibleTradeTarget;

/* package */ class FungibleTradeTargetFormatter extends AbstractFormatter<FungibleTradeTarget> {

  private static final String NAME = "name";
  private static final String ID = "id";

  /* package */  FungibleTradeTargetFormatter() {
    super(FungibleTradeTarget.class);
  }

  @Override
  public Object formatCell(FungibleTradeTarget trade, ValueSpecification valueSpec) {
    return ImmutableMap.of(NAME, trade.getName(), ID, trade.getId().getObjectId());
  }

  @Override
  public DataType getDataType() {
    return DataType.FUNGIBLE_TRADE;
  }
}
