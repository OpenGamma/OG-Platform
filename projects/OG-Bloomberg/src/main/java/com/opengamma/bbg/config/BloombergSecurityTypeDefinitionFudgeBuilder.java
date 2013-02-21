/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.config;

import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.bbg.loader.SecurityType;
import com.opengamma.id.UniqueId;

/**
 * Builder for converting {@link BloombergSecurityTypeDefinition} to/from Fudge messages.
 */
@FudgeBuilderFor(BloombergSecurityTypeDefinition.class)
public class BloombergSecurityTypeDefinitionFudgeBuilder implements FudgeBuilder<BloombergSecurityTypeDefinition> {
  
  private static final String TYPES_FLD = "types";
  private static final String SEC_TYPE_FLD = "sectype";
  private static final String UNIQUE_ID_FLD = "id";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergSecurityTypeDefinition object) {
    MutableFudgeMsg rootMsg = serializer.newMessage();
    if (object.getUniqueId() != null) {
      serializer.addToMessage(rootMsg, UNIQUE_ID_FLD, null, object.getUniqueId());
    }
    
    Collection<SecurityType> allSecurityTypes = object.getAllSecurityTypes();
    if (!allSecurityTypes.isEmpty()) {
      MutableFudgeMsg typesMsg = serializer.newMessage();
      for (SecurityType securityType : allSecurityTypes) {
        Collection<String> validTypes = object.getValidTypes(securityType);
        MutableFudgeMsg secTypeMsg = serializer.newMessage();
        for (String type : validTypes) {
          secTypeMsg.add(SEC_TYPE_FLD, type);
        }
        serializer.addToMessage(typesMsg, securityType.name(), null, secTypeMsg);
      }
      serializer.addToMessage(rootMsg, TYPES_FLD, null, typesMsg);
    }
    return rootMsg;
  }

  @Override
  public BloombergSecurityTypeDefinition buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    BloombergSecurityTypeDefinition definition = new BloombergSecurityTypeDefinition();
    
    if (message.hasField(UNIQUE_ID_FLD)) {
      definition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, message.getByName(UNIQUE_ID_FLD)));
    }   
    FudgeMsg typesMsg = message.getMessage(TYPES_FLD);
    if (typesMsg != null) {
      for (String securityTypeName : typesMsg.getAllFieldNames()) {
        FudgeMsg msg = (FudgeMsg) typesMsg.getByName(securityTypeName).getValue();
        for (FudgeField field : msg.getAllByName(SEC_TYPE_FLD)) {
          String secType = (String) field.getValue();
          definition.addSecurityType(secType, SecurityType.valueOf(securityTypeName));
        }
      }
    }
    return definition;
  }

}
