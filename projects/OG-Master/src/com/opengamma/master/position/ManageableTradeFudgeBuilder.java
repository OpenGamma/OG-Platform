/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.position.Trade;
import com.opengamma.core.position.TradeFudgeBuilder;

/**
 * Fudge builder for {@link ManageableTrade} delegating to {@link TradeFudgeBuilder}.
 */
@FudgeBuilderFor(ManageableTrade.class)
public class ManageableTradeFudgeBuilder implements FudgeBuilder<ManageableTrade> {

  private final TradeFudgeBuilder _delegate = new TradeFudgeBuilder();

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ManageableTrade object) {
    return _delegate.buildMessage(serializer, object);
  }

  @Override
  public ManageableTrade buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Trade trade = _delegate.buildObject(deserializer, message);
    final ManageableTrade manageableTrade = new ManageableTrade(trade);
    manageableTrade.setUniqueId(trade.getUniqueId());
    return manageableTrade;
  }

}
