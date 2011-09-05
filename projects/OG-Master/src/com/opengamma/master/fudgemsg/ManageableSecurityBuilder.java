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

import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBundleBuilder;
import com.opengamma.util.fudgemsg.UniqueIdBuilder;

/**
 * A Fudge builder for {@code ManageableSecurity}.
 */
@FudgeBuilderFor(ManageableSecurity.class)
public class ManageableSecurityBuilder extends AbstractFudgeBuilder implements FudgeBuilder<ManageableSecurity> {

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
    ManageableSecurityBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, ManageableSecurity object, final MutableFudgeMsg msg) {
    addToMessage(msg, UNIQUE_ID_KEY, UniqueIdBuilder.toFudgeMsg(serializer, object.getUniqueId()));
    addToMessage(msg, NAME_KEY, object.getName());
    addToMessage(msg, SECURITY_TYPE_KEY, object.getSecurityType());
    addToMessage(msg, IDENTIFIERS_KEY, ExternalIdBundleBuilder.toFudgeMsg(serializer, object.getExternalIdBundle()));
  }

  @Override
  public ManageableSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ManageableSecurity object = new ManageableSecurity();
    ManageableSecurityBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, ManageableSecurity object) {
    object.setUniqueId(UniqueIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNIQUE_ID_KEY)));
    object.setName(msg.getString(NAME_KEY));
    object.setSecurityType(msg.getString(SECURITY_TYPE_KEY));
    object.setExternalIdBundle(ExternalIdBundleBuilder.fromFudgeMsg(deserializer, msg.getMessage(IDENTIFIERS_KEY)));
  }

}
