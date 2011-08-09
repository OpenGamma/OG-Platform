/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * A request to update a view definition.
 */
public final class UpdateViewDefinitionRequest {

  /**
   * The view definition name
   */
  private String _name;
  /**
   * The view definition
   */
  private ViewDefinition _viewDefinition;
  
  /**
   * Creates an instance
   */
  public UpdateViewDefinitionRequest() {
  }
  
  /**
   * Creates an instance
   * 
   * @param viewDefinition  the view definition
   */
  public UpdateViewDefinitionRequest(ViewDefinition viewDefinition) {
    setViewDefinition(viewDefinition);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name field.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name field, not null
   * @param name  the name
   */
  public void setName(String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
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
   * Sets the viewDefinition field, not null
   * @param viewDefinition  the viewDefinition
   */
  public void setViewDefinition(ViewDefinition viewDefinition) {
    ArgumentChecker.notNull(viewDefinition, "viewDefinition");
    _viewDefinition = viewDefinition;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    ArgumentChecker.notNull(getName(), "Name must not be null");
    ArgumentChecker.notNull(getViewDefinition(), "View definition must not be null");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String NAME_FIELD_NAME = "name";
  /** Field name. */
  private static final String VIEW_DEFINITION_FIELD_NAME = "viewDefinition";

  /**
   * Serializes to a Fudge message.
   * @param serializer  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    if (_name != null) {
      msg.add(NAME_FIELD_NAME, _name);
    }
    if (_viewDefinition != null) {
      serializer.addToMessage(msg, VIEW_DEFINITION_FIELD_NAME, null, _viewDefinition);
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param deserializer  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static UpdateViewDefinitionRequest fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    UpdateViewDefinitionRequest req = new UpdateViewDefinitionRequest();
    if (msg.hasField(NAME_FIELD_NAME)) {
      req.setName(msg.getString(NAME_FIELD_NAME));
    }
    if (msg.hasField(VIEW_DEFINITION_FIELD_NAME)) {
      req.setViewDefinition(deserializer.fieldValueToObject(ViewDefinition.class, msg.getByName(VIEW_DEFINITION_FIELD_NAME)));
    }
    return req;
  }
  
}
