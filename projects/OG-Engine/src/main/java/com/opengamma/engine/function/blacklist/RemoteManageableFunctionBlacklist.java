/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * Provides remote access to a {@link ManageableFunctionBlacklist}.
 */
public class RemoteManageableFunctionBlacklist extends RemoteFunctionBlacklist implements ManageableFunctionBlacklist {

  public RemoteManageableFunctionBlacklist(final FudgeDeserializer fdc, final FudgeMsg info, final RemoteManageableFunctionBlacklistProvider provider) {
    super(fdc, info, provider);
  }

  @Override
  protected RemoteManageableFunctionBlacklistProvider getProvider() {
    return (RemoteManageableFunctionBlacklistProvider) super.getProvider();
  }

  @Override
  public void addBlacklistRule(final FunctionBlacklistRule rule) {
    final FudgeSerializer fsc = new FudgeSerializer(getProvider().getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    fsc.addToMessage(msg, DataManageableFunctionBlacklistResource.RULE_FIELD, null, rule);
    getProvider().add(getName(), msg);
  }

  @Override
  public void addBlacklistRule(final FunctionBlacklistRule rule, final int timeToLive) {
    final FudgeSerializer fsc = new FudgeSerializer(getProvider().getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    msg.add(DataManageableFunctionBlacklistResource.TTL_FIELD, null, FudgeWireType.LONG, timeToLive);
    fsc.addToMessage(msg, DataManageableFunctionBlacklistResource.RULE_FIELD, null, rule);
    getProvider().add(getName(), msg);
  }

  @Override
  public void addBlacklistRules(final Collection<FunctionBlacklistRule> rules) {
    final FudgeSerializer fsc = new FudgeSerializer(getProvider().getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    for (FunctionBlacklistRule rule : rules) {
      fsc.addToMessage(msg, DataManageableFunctionBlacklistResource.RULE_FIELD, null, rule);
    }
    getProvider().add(getName(), msg);
  }

  @Override
  public void addBlacklistRules(final Collection<FunctionBlacklistRule> rules, final int timeToLive) {
    final FudgeSerializer fsc = new FudgeSerializer(getProvider().getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    msg.add(DataManageableFunctionBlacklistResource.TTL_FIELD, null, FudgeWireType.LONG, timeToLive);
    for (FunctionBlacklistRule rule : rules) {
      fsc.addToMessage(msg, DataManageableFunctionBlacklistResource.RULE_FIELD, null, rule);
    }
    getProvider().add(getName(), msg);
  }

  @Override
  public void removeBlacklistRule(final FunctionBlacklistRule rule) {
    final FudgeSerializer fsc = new FudgeSerializer(getProvider().getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    fsc.addToMessage(msg, DataManageableFunctionBlacklistResource.RULE_FIELD, null, rule);
    getProvider().remove(getName(), msg);
  }

  @Override
  public void removeBlacklistRules(final Collection<FunctionBlacklistRule> rules) {
    final FudgeSerializer fsc = new FudgeSerializer(getProvider().getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    for (FunctionBlacklistRule rule : rules) {
      fsc.addToMessage(msg, DataManageableFunctionBlacklistResource.RULE_FIELD, null, rule);
    }
    getProvider().remove(getName(), msg);
  }

}
