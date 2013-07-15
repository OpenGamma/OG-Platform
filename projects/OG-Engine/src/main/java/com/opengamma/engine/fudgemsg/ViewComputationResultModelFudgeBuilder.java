/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.InMemoryViewResultModel;

/**
 */
@GenericFudgeBuilderFor(ViewComputationResultModel.class)
public class ViewComputationResultModelFudgeBuilder extends ViewResultModelFudgeBuilder implements FudgeBuilder<ViewComputationResultModel> {

  private static final String FIELD_LIVEDATA = "liveData";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ViewComputationResultModel resultModel) {
    final MutableFudgeMsg message = ViewResultModelFudgeBuilder.createResultModelMessage(serializer, resultModel);
    // Prevent subclass headers from being added to the message later, ensuring that this builder will be used for deserialization
    FudgeSerializer.addClassHeader(message, ViewComputationResultModel.class);
    final MutableFudgeMsg liveDataMsg = message.addSubMessage(FIELD_LIVEDATA, null);
    for (final ComputedValue value : resultModel.getAllMarketData()) {
      serializer.addToMessage(liveDataMsg, null, 1, value);
    }
    return message;
  }

  @Override
  public ViewComputationResultModel buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final InMemoryViewComputationResultModel resultModel = (InMemoryViewComputationResultModel) bootstrapCommonDataFromMessage(deserializer, message);
    for (final FudgeField field : message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_LIVEDATA))) {
      final ComputedValue liveData = deserializer.fieldValueToObject(ComputedValue.class, field);
      resultModel.addMarketData(liveData);
    }
    return resultModel;
  }

  @Override
  protected InMemoryViewResultModel constructImpl() {
    return new InMemoryViewComputationResultModel();
  }

}
