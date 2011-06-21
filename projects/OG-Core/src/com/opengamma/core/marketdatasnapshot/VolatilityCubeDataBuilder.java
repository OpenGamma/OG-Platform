/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * 
 */
@FudgeBuilderFor(VolatilityCubeData.class)
public class VolatilityCubeDataBuilder implements FudgeBuilder<VolatilityCubeData> {

  private static final String DATA_POINTS_FIELD_NAME = "dataPoints";
  private static final String OTHER_DATA_FIELD_NAME = "otherData";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, VolatilityCubeData object) {
    MutableFudgeMsg ret = context.newMessage();
    FudgeSerializationContext.addClassHeader(ret, VolatilityCubeData.class);
    
    context.addToMessage(ret, DATA_POINTS_FIELD_NAME, null, object.getDataPoints());
    context.addToMessage(ret, OTHER_DATA_FIELD_NAME, null, object.getOtherData());
    
    return ret;
  }

  
  @Override
  public VolatilityCubeData buildObject(FudgeDeserializationContext context, FudgeMsg message) {

    Class<?> mapClass = (Class<?>) Map.class;
    FudgeField pointsField = message.getByName(DATA_POINTS_FIELD_NAME);
    
    @SuppressWarnings("unchecked")
    Map<VolatilityPoint, Double> dataPoints = (Map<VolatilityPoint, Double>) (pointsField == null ? null : context.fieldValueToObject(mapClass, pointsField));
    FudgeField otherField = message.getByName(OTHER_DATA_FIELD_NAME);
    SnapshotDataBundle otherData = otherField == null ? null : context.fieldValueToObject(SnapshotDataBundle.class, otherField);
    
    VolatilityCubeData ret = new VolatilityCubeData();
    ret.setDataPoints(dataPoints);
    ret.setOtherData(otherData);
    return ret;
  }

}
