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

  /** Field name. */
  public static final String DATE_FIELD_NAME = "observationDate";
  /** Field name. */
  public static final String TIME_FIELD_NAME = "observationTime";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BatchId object) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(DATE_FIELD_NAME, null, object.getObservationDate());
    msg.add(TIME_FIELD_NAME, null, object.getObservationTime());
    return msg;
  }

  @Override
  public BatchId buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField dateField = message.getByName(DATE_FIELD_NAME);
    FudgeField timeField = message.getByName(TIME_FIELD_NAME);

    Validate.notNull(dateField, "Fudge message is not a BatchId - field " + DATE_FIELD_NAME + " is not present");
    Validate.notNull(timeField, "Fudge message is not a BatchId - field " + TIME_FIELD_NAME + " is not present");

    LocalDate date = message.getFieldValue(LocalDate.class, dateField);
    String time = message.getFieldValue(String.class, timeField);
    
    return new BatchId(date, time);
  }

}
