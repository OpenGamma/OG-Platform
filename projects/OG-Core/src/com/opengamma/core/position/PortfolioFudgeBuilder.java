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
  public static final String IDENTIFIER_FIELD_NAME = "identifier";
  /** Field name. */
  public static final String NAME_FIELD_NAME = "name";
  /** Field name. */
  public static final String ROOT_FIELD_NAME = "root";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Portfolio portfolio) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, IDENTIFIER_FIELD_NAME, null, portfolio.getUniqueId());
    message.add(NAME_FIELD_NAME, portfolio.getName());
    serializer.addToMessage(message, ROOT_FIELD_NAME, null, portfolio.getRootNode());
    return message;
  }

  @Override
  public Portfolio buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField idField = message.getByName(IDENTIFIER_FIELD_NAME);
    final UniqueId id = idField != null ? deserializer.fieldValueToObject(UniqueId.class, idField) : null;
    final String name = message.getFieldValue(String.class, message.getByName(NAME_FIELD_NAME));
    final PortfolioNode node = deserializer.fieldValueToObject(PortfolioNode.class, message.getByName(ROOT_FIELD_NAME));
    
    SimplePortfolio portfolio = new SimplePortfolio(name);
    if (id != null) {
      portfolio.setUniqueId(id);
    }
    portfolio.setRootNode((SimplePortfolioNode) node);
    return portfolio;
  }

}
