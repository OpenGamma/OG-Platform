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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;

/**
 * A Fudge builder for {@code ManageableSecurity}.
 */
@FudgeBuilderFor(ManageableSecurity.class)
public class ManageableSecurityBuilder implements FudgeBuilder<ManageableSecurity> {

  /** Field name. */
  public static final String UNIQUE_ID_KEY = "uniqueId";
  /** Field name. */
  public static final String NAME_KEY = "name";
  /** Field name. */
  public static final String SECURITY_TYPE_KEY = "securityType";
  /** Field name. */
  public static final String IDENTIFIERS_KEY = "identifiers";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ManageableSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ManageableSecurityBuilder.buildMessage(serializer, object, msg);
    return msg;
  }

  public static void buildMessage(FudgeSerializer serializer, ManageableSecurity object, final MutableFudgeMsg msg) {
    if (object.getUniqueId() != null) {
      serializer.addToMessage(msg, UNIQUE_ID_KEY, null, object.getUniqueId().toFudgeMsg(serializer));
    }
    serializer.addToMessage(msg, NAME_KEY, null, object.getName());
    serializer.addToMessage(msg, SECURITY_TYPE_KEY, null, object.getSecurityType());
    serializer.addToMessage(msg, IDENTIFIERS_KEY, null, object.getIdentifiers().toFudgeMsg(serializer));
  }

  @Override
  public ManageableSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ManageableSecurity object = new ManageableSecurity("");
    ManageableSecurityBuilder.buildObject(deserializer, msg, object);
    return object;
  }

  public static void buildObject(FudgeDeserializer deserializer, FudgeMsg msg, ManageableSecurity object) {
    FudgeMsg uniqueIdField = msg.getMessage(UNIQUE_ID_KEY);
    object.setUniqueId(uniqueIdField != null ? UniqueId.fromFudgeMsg(deserializer, uniqueIdField) : null);
    object.setName(msg.getString(NAME_KEY));
    object.setSecurityType(msg.getString(SECURITY_TYPE_KEY));
    object.setIdentifiers(ExternalIdBundle.fromFudgeMsg(deserializer, msg.getMessage(IDENTIFIERS_KEY)));
  }

}
