/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;

public class PortfolioBuilder implements FudgeBuilder<Portfolio> {
  
  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, Portfolio portfolio) {
    final MutableFudgeFieldContainer msg = context.newMessage ();
    PortfolioNodeBuilder.addPortfolioNodeFields (context, msg, portfolio);
    return msg;
  }
  
  @Override
  public Portfolio buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    // Portfolio
    final String name = message.getFieldValue (String.class, message.getByName (PortfolioNodeBuilder.FIELD_NAME));
    final PortfolioImpl portfolio = new PortfolioImpl (name);
    // PortfolioNode
    PortfolioNodeBuilder.readPortfolioNodeFields (context, message, portfolio);
    return portfolio;
  }
  
  
}