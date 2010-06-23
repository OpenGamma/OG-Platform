/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.position.PortfolioNode;

/**
 * Fudge message builder for {@link AddPortfolioNodeRequest}.
 */
public class AddPortfolioRequestBuilder implements FudgeBuilder<AddPortfolioRequest> {

  private static final String FIELD_NAME = "name";
  private static final String FIELD_ROOT_NODE = "rootNode";
  
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, AddPortfolioRequest addPortfolioRequest) {
    MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(FIELD_NAME, addPortfolioRequest.getName());
    context.objectToFudgeMsg(msg, FIELD_ROOT_NODE, null, addPortfolioRequest.getRootNode());
    return msg;
  }

  @Override
  public AddPortfolioRequest buildObject(FudgeDeserializationContext context, FudgeFieldContainer msg) {
    String name = msg.getString(FIELD_NAME);
    FudgeField rootNodeField = msg.getByName(FIELD_ROOT_NODE);
    PortfolioNode rootNode = rootNodeField != null ? context.fieldValueToObject(PortfolioNode.class, rootNodeField) : null;
    AddPortfolioRequest addPortfolioRequest = new AddPortfolioRequest();
    if (name != null) {
      addPortfolioRequest.setName(name);
    }
    if (rootNode != null) {
      addPortfolioRequest.setRootNode(rootNode);
    }
    return addPortfolioRequest;
  }
  
}
