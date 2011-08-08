/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;

/**
 * Fudge message builder (serializer/deserializer) for SQLDateDoubleTimeSeries.
 */
public class SQLDateDoubleTimeSeriesBuilder implements FudgeBuilder<SQLDateDoubleTimeSeries> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SQLDateDoubleTimeSeries object) {
    return null;
  }

  @Override
  public SQLDateDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return null;
  }

}
