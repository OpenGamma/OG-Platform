/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.calcnode.CalculationJobSpecification;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.id.VersionCorrection;

/**
 * Fudge message builder for {@code CalculationJob}.
 * 
 * <pre>
 * message CalculationJob extends CalculationJobSpecification, CacheSelect {
 *   optional long[] required;                              // pre-requisite job identifiers
 *   required long functionInitId;                          // function initialization latch flag
 *   required VersionCorrection versionCorrection;          // resolver version/correction timestamps
 *   required CalculationJobItem[] items;                   // job items
 * }
 * </pre>
 */
@FudgeBuilderFor(CalculationJob.class)
public class CalculationJobFudgeBuilder implements FudgeBuilder<CalculationJob> {

  private static final String REQUIRED_FIELD_NAME = "required";
  private static final String FUNCTION_INITIALIZATION_IDENTIFIER_FIELD_NAME = "functionInitId";
  private static final String RESOLVER_VERSION_CORRECTION_FIELD_NAME = "versionCorrection";
  private static final String ITEMS_FIELD_NAME = "items";

  protected FudgeMsg buildItemsMessage(final FudgeSerializer serializer, final List<CalculationJobItem> items) {
    final MutableFudgeMsg msg = serializer.newMessage();
    final Map<ComputationTargetSpecification, Integer> targets = new HashMap<ComputationTargetSpecification, Integer>();
    final Map<String, Integer> functions = new HashMap<String, Integer>();
    final Map<FunctionParameters, Integer> parameters = new HashMap<FunctionParameters, Integer>();
    for (CalculationJobItem item : items) {
      msg.add(null, null, CalculationJobItemFudgeBuilder.buildMessageImpl(serializer, item, targets, functions, parameters));
    }
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CalculationJob object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CalculationJobSpecificationFudgeBuilder.buildMessageImpl(msg, object.getSpecification());
    CacheSelectHintFudgeBuilder.buildMessageImpl(msg, object.getCacheSelectHint());
    if (object.getRequiredJobIds() != null) {
      msg.add(REQUIRED_FIELD_NAME, object.getRequiredJobIds());
    }
    msg.add(FUNCTION_INITIALIZATION_IDENTIFIER_FIELD_NAME, object.getFunctionInitializationIdentifier());
    serializer.addToMessage(msg, RESOLVER_VERSION_CORRECTION_FIELD_NAME, null, object.getResolverVersionCorrection());
    msg.add(ITEMS_FIELD_NAME, buildItemsMessage(serializer, object.getJobItems()));
    return msg;
  }

  protected List<CalculationJobItem> buildItemsObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final List<CalculationJobItem> result = new ArrayList<CalculationJobItem>(msg.getNumFields());
    final Map<Integer, ComputationTargetSpecification> targets = new HashMap<Integer, ComputationTargetSpecification>();
    final Map<Integer, String> functions = new HashMap<Integer, String>();
    final Map<Integer, FunctionParameters> parameters = new HashMap<Integer, FunctionParameters>();
    for (FudgeField field : msg) {
      result.add(CalculationJobItemFudgeBuilder.buildObjectImpl(deserializer, (FudgeMsg) field.getValue(), targets, functions, parameters));
    }
    return result;
  }

  @Override
  public CalculationJob buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final CalculationJobSpecification jobSpec = CalculationJobSpecificationFudgeBuilder.buildObjectImpl(message);
    final CacheSelectHint cacheSelectHint = CacheSelectHintFudgeBuilder.buildObjectImpl(message);
    final long[] requiredJobIds = message.getValue(long[].class, REQUIRED_FIELD_NAME);
    final long functionInitializationIdentifier = message.getLong(FUNCTION_INITIALIZATION_IDENTIFIER_FIELD_NAME);
    final VersionCorrection resolverVersionCorrection = deserializer.fieldValueToObject(VersionCorrection.class, message.getByName(RESOLVER_VERSION_CORRECTION_FIELD_NAME));
    final List<CalculationJobItem> jobItems = buildItemsObject(deserializer, message.getMessage(ITEMS_FIELD_NAME));
    return new CalculationJob(jobSpec, functionInitializationIdentifier, resolverVersionCorrection, requiredJobIds, jobItems, cacheSelectHint);
  }

}
