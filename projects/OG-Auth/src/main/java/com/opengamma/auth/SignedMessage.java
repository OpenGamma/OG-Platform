package com.opengamma.auth;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class SignedMessage<T> {

  final private T _message;
  final private String _signature;

  private SignedMessage(T message, String signature) {
    _message = message;
    _signature = signature;
  }

  public static <T> SignedMessage<T> of(T message, String signature) {
    return new SignedMessage(message, signature);
  }

  public T getMessage() {
    return _message;
  }

  public String getSignature() {
    return _signature;
  }


  @FudgeBuilderFor(SignedMessage.class)
  public static class FudgeBuilder implements org.fudgemsg.mapping.FudgeBuilder<SignedMessage> {

    private static final String MESSAGE = "message";
    private static final String SIGNATURE = "signature";


    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SignedMessage object) {
      MutableFudgeMsg rootMsg = serializer.newMessage();
      serializer.addToMessageWithClassHeaders(rootMsg, MESSAGE, null, object.getMessage());
      serializer.addToMessage(rootMsg, SIGNATURE, null, object.getSignature());
      return rootMsg;
    }

    @Override
    public SignedMessage buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      Object msg = deserializer.fieldValueToObject(message.getByName(MESSAGE));
      String signature = deserializer.fieldValueToObject(String.class, message.getByName(SIGNATURE));
      return new SignedMessage(msg, signature);
    }
  }
}
