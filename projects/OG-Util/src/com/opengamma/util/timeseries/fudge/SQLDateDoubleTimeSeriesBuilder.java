/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;

/**
 * Fudge message builder (serializer/deserializer) for SQLDateDoubleTimeSeries.
 */
public class SQLDateDoubleTimeSeriesBuilder implements FudgeBuilder<SQLDateDoubleTimeSeries> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, SQLDateDoubleTimeSeries object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SQLDateDoubleTimeSeries buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    // TODO Auto-generated method stub
    return null;
  }

}
