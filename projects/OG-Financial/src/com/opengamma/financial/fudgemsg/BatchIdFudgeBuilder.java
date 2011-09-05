/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.batch.BatchId;

/**
 * Fudge message builder for {@code BatchId}.
 */
@FudgeBuilderFor(BatchId.class)
public class BatchIdFudgeBuilder implements FudgeBuilder<BatchId> {
  /**
   * Fudge field name.
   */
  private static final String DATE_KEY = "observationDate";
  /**
   * Fudge field name.
   */
  private static final String TIME_KEY = "observationTime";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BatchId object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(DATE_KEY, null, object.getObservationDate());
    msg.add(TIME_KEY, null, object.getObservationTime());
    return msg;
  }

  @Override
  public BatchId buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField dateField = message.getByName(DATE_KEY);
    FudgeField timeField = message.getByName(TIME_KEY);

    Validate.notNull(dateField, "Fudge message is not a BatchId - field " + DATE_KEY + " is not present");
    Validate.notNull(timeField, "Fudge message is not a BatchId - field " + TIME_KEY + " is not present");

    LocalDate date = message.getFieldValue(LocalDate.class, dateField);
    String time = message.getFieldValue(String.class, timeField);
    
    return new BatchId(date, time);
  }

}
