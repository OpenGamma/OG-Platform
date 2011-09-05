/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.id.UniqueId;

/**
 * Fudge message builder for {@code Portfolio}.
 */
@GenericFudgeBuilderFor(Portfolio.class)
public class PortfolioFudgeBuilder implements FudgeBuilder<Portfolio> {

  /** Field name. */
  public static final String FIELD_IDENTIFIER = "identifier";
  /** Field name. */
  public static final String FIELD_NAME = "name";
  /** Field name. */
  public static final String FIELD_ROOT = "root";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Portfolio portfolio) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, FIELD_IDENTIFIER, null, portfolio.getUniqueId());
    message.add(FIELD_NAME, portfolio.getName());
    serializer.addToMessage(message, FIELD_ROOT, null, portfolio.getRootNode());
    return message;
  }

  @Override
  public Portfolio buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField idField = message.getByName(FIELD_IDENTIFIER);
    final UniqueId id = idField != null ? deserializer.fieldValueToObject(UniqueId.class, idField) : null;
    final String name = message.getFieldValue(String.class, message.getByName(FIELD_NAME));
    final PortfolioNode node = deserializer.fieldValueToObject(PortfolioNode.class, message.getByName(FIELD_ROOT));
    
    SimplePortfolio portfolio = new SimplePortfolio(name);
    if (id != null) {
      portfolio.setUniqueId(id);
    }
    portfolio.setRootNode((SimplePortfolioNode) node);
    return portfolio;
  }

}
