/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalIdBundleFudgeBuilder;
import com.opengamma.id.UniqueIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code ManageableSecurity}.
 */
@FudgeBuilderFor(ManageableSecurity.class)
public class ManageableSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ManageableSecurity> {

  /** Field name. */
  public static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  /** Field name. */
  public static final String NAME_FIELD_NAME = "name";
  /** Field name. */
  public static final String SECURITY_TYPE_FIELD_NAME = "securityType";
  /** Field name. */
  public static final String IDENTIFIERS_FIELD_NAME = "identifiers";
  /** Field name. */
  public static final String ATTRIBUTES_FIELD_NAME = "attributes";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ManageableSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    ManageableSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, ManageableSecurity object, final MutableFudgeMsg msg) {
    addToMessage(msg, UNIQUE_ID_FIELD_NAME, UniqueIdFudgeBuilder.toFudgeMsg(serializer, object.getUniqueId()));
    addToMessage(msg, NAME_FIELD_NAME, object.getName());
    addToMessage(msg, SECURITY_TYPE_FIELD_NAME, object.getSecurityType());
    addToMessage(msg, IDENTIFIERS_FIELD_NAME, ExternalIdBundleFudgeBuilder.toFudgeMsg(serializer, object.getExternalIdBundle()));
    addToMessage(msg, ATTRIBUTES_FIELD_NAME, serializer.objectToFudgeMsg(object.getAttributes()));
  }

  @Override
  public ManageableSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ManageableSecurity object = new ManageableSecurity();
    ManageableSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  @SuppressWarnings("unchecked")
  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, ManageableSecurity object) {
    object.setUniqueId(UniqueIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNIQUE_ID_FIELD_NAME)));
    object.setName(msg.getString(NAME_FIELD_NAME));
    object.setSecurityType(msg.getString(SECURITY_TYPE_FIELD_NAME));
    object.setExternalIdBundle(ExternalIdBundleFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(IDENTIFIERS_FIELD_NAME)));
    object.setAttributes((Map<String, String>) deserializer.fieldValueToObject(msg.getByName(ATTRIBUTES_FIELD_NAME)));
  }

}
