/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Fudge message builder for {@code UserPrincipal}.
 */
@FudgeBuilderFor(UserPrincipal.class)
public class UserPrincipalFudgeBuilder implements FudgeBuilder<UserPrincipal> {

  /** Field name. */
  public static final String USER_NAME_FIELD_NAME = "userName";
  /** Field name. */
  public static final String IP_ADDRESS_FIELD_NAME = "ipAddress";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, UserPrincipal object) {
    return UserPrincipalFudgeBuilder.toFudgeMsg(serializer, object);
  }

  public static MutableFudgeMsg toFudgeMsg(FudgeSerializer serializer, UserPrincipal object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    UserPrincipalFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, UserPrincipal object, final MutableFudgeMsg msg) {
    if (object.getUserName() != null) {
      msg.add(USER_NAME_FIELD_NAME, null, object.getUserName());
    }
    if (object.getIpAddress() != null) {
      msg.add(IP_ADDRESS_FIELD_NAME, null, object.getIpAddress());
    }
  }

  @Override
  public UserPrincipal buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return UserPrincipalFudgeBuilder.fromFudgeMsg(deserializer, msg);
  }

  public static UserPrincipal fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    String userName = msg.getString(USER_NAME_FIELD_NAME);
    String ipAddress = msg.getString(IP_ADDRESS_FIELD_NAME);
    return new UserPrincipal(userName, ipAddress);
  }

}
