/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * A request to add a view definition.
 */
public final class AddViewDefinitionRequest {
  
  /**
   * The view definition
   */
  private ViewDefinition _viewDefinition;
  
  /**
   * Creates an instance.
   */
  public AddViewDefinitionRequest() {
  }
  
  /**
   * Creates an instance.
   * 
   * @param viewDefinition  the view definition, not null
   */
  public AddViewDefinitionRequest(ViewDefinition viewDefinition) {
    setViewDefinition(viewDefinition);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the viewDefinition field.
   * @return the viewDefinition
   */
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  /**
   * Sets the viewDefinition field.
   * @param viewDefinition  the view definition, not null
   */
  public void setViewDefinition(ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewDefinition, "view definition");
    _viewDefinition = viewDefinition;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    ArgumentChecker.notNull(getViewDefinition(), "View definition must not be null");
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String VIEW_DEFINITION_FIELD_NAME = "viewDefinition";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    if (_viewDefinition != null) {
      context.objectToFudgeMsg(msg, VIEW_DEFINITION_FIELD_NAME, null, _viewDefinition);
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static AddViewDefinitionRequest fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    AddViewDefinitionRequest req = new AddViewDefinitionRequest();
    if (msg.hasField(VIEW_DEFINITION_FIELD_NAME)) {
      req.setViewDefinition(context.fieldValueToObject(ViewDefinition.class, msg.getByName(VIEW_DEFINITION_FIELD_NAME)));
    }
    return req;
  }
  
}
