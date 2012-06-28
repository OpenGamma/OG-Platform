/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.engine.function.blacklist.DefaultFunctionBlacklistPolicy;
import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy;
import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy.Entry;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdFudgeSecondaryType;

/**
 * Fudge builder for {@link FunctionBlacklistPolicy}.
 * 
 * <pre>
 * message FunctionBlacklistPolicy {
 *   required UniqueId uniqueId;                // unique identifier
 *   required string name;                      // symbolic name
 *   required long ttl;                         // default entry activation period
 *   repeated Entry entry;                      // entries
 *   
 *   message Entry {
 *     optional long ttl;                       // ttl if different to the policy
 *     optional indicator functionIdentifier;   // match on function identifier
 *     optional indicator functionParameters;   // match on function parameters
 *     optional indicator target;               // match on target
 *     optional indicator inputs;               // match on function inputs
 *     optional indicator outputs;              // match on function outputs
 *   }
 *   
 * }
 * </pre>
 */
@GenericFudgeBuilderFor(FunctionBlacklistPolicy.class)
public class FunctionBlacklistPolicyFudgeBuilder implements FudgeBuilder<FunctionBlacklistPolicy> {

  /**
   * Fudge builder for {@link Entry}.
   */
  @FudgeBuilderFor(Entry.class)
  public static class EntryFudgeBuilder implements FudgeBuilder<Entry> {

    public static void buildMessageImpl(final MutableFudgeMsg msg, final Entry object) {
      if (!object.isDefaultActivationPeriod()) {
        msg.add("ttl", null, FudgeWireType.LONG, object.getActivationPeriod());
      }
      if (object.isMatchFunctionIdentifier()) {
        msg.add("functionIdentifier", null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      }
      if (object.isMatchFunctionParameters()) {
        msg.add("functionParameters", null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      }
      if (object.isMatchTarget()) {
        msg.add("target", null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      }
      if (object.isMatchInputs()) {
        msg.add("inputs", null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      }
      if (object.isMatchOutputs()) {
        msg.add("outputs", null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      }
    }

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Entry object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      buildMessageImpl(msg, object);
      return msg;
    }

    public static Entry buildObjectImpl(final FudgeMsg msg) {
      Entry e = Entry.WILDCARD;
      final Integer ttl = msg.getInt("ttl");
      if (ttl != null) {
        e = e.activationPeriod(ttl);
      }
      if (msg.hasField("functionIdentifier")) {
        e = e.matchFunctionIdentifier();
      }
      if (msg.hasField("functionParameters")) {
        e = e.matchFunctionParameters();
      }
      if (msg.hasField("target")) {
        e = e.matchTarget();
      }
      if (msg.hasField("inputs")) {
        e = e.matchInputs();
      }
      if (msg.hasField("outputs")) {
        e = e.matchOutputs();
      }
      return e;
    }

    @Override
    public Entry buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
      return buildObjectImpl(msg);
    }

  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FunctionBlacklistPolicy object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("uniqueId", null, UniqueIdFudgeSecondaryType.INSTANCE, object.getUniqueId());
    msg.add("name", null, FudgeWireType.STRING, object.getName());
    msg.add("ttl", null, FudgeWireType.LONG, object.getDefaultEntryActivationPeriod());
    for (Entry entry : object.getEntries()) {
      final MutableFudgeMsg entryMsg = msg.addSubMessage("entry", null);
      EntryFudgeBuilder.buildMessageImpl(entryMsg, entry);
    }
    return msg;
  }

  @Override
  public FunctionBlacklistPolicy buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final UniqueId uniqueId = msg.getValue(UniqueId.class, "uniqueId");
    final String name = msg.getString("name");
    final int ttl = msg.getInt("ttl");
    final List<FudgeField> entryFields = msg.getAllByName("entry");
    final List<Entry> entries = new ArrayList<Entry>(entryFields.size());
    for (FudgeField entryField : entryFields) {
      entries.add(EntryFudgeBuilder.buildObjectImpl((FudgeMsg) entryField.getValue()));
    }
    return new DefaultFunctionBlacklistPolicy(uniqueId, name, ttl, entries);
  }

}
