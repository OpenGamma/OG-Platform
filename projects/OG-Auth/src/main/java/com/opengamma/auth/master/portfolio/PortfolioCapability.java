package com.opengamma.auth.master.portfolio;

import com.opengamma.auth.Capability;
import com.opengamma.auth.SignedMessage;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class PortfolioCapability extends Capability<PortfolioEntitlement> {

  public PortfolioCapability() {
  }

  public static PortfolioCapability of(Collection<SignedMessage<PortfolioEntitlement>> messages) {
    PortfolioCapability capability = new PortfolioCapability();
    capability._messages.addAll(messages);
    return capability;
  }

  @SafeVarargs
  public static PortfolioCapability of(SignedMessage<PortfolioEntitlement>... messages) {
    return PortfolioCapability.of(Arrays.asList(messages));
  }

  @FudgeBuilderFor(PortfolioCapability.class)
  public static class FudgeBuilder implements org.fudgemsg.mapping.FudgeBuilder<PortfolioCapability> {

    private static final String MESSAGES = "messages";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PortfolioCapability object) {
      MutableFudgeMsg rootMsg = serializer.newMessage();
      serializer.addToMessage(rootMsg, MESSAGES, null, object._messages);
      return rootMsg;
    }

    @Override
    public PortfolioCapability buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      return PortfolioCapability.of((Collection<SignedMessage<PortfolioEntitlement>>) deserializer.fieldValueToObject(message.getByName(MESSAGES)));
    }
  }
}
