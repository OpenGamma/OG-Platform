/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.fudgemsg.TradeBuilder;
import com.opengamma.master.position.ManageableTrade;

/**
 * Fudge builder for {@link ManageableTrade} delegating to {@link TradeBuilder}.
 */
@FudgeBuilderFor(ManageableTrade.class)
public class ManageableTradeBuilder implements FudgeBuilder<ManageableTrade> {

  private final TradeBuilder _delegate = new TradeBuilder();

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final ManageableTrade object) {
    return _delegate.buildMessage(context, object);
  }

  @Override
  public ManageableTrade buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    return new ManageableTrade(_delegate.buildObject(context, message));
  }

}
