/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.batch.BatchId;

/**
 * Fudge message builder for {@code BatchId}.
 */
@FudgeBuilderFor(BatchId.class)
public class BatchIdBuilder implements FudgeBuilder<BatchId> {
  /**
   * Fudge field name.
   */
  private static final String DATE_KEY = "observationDate";
  /**
   * Fudge field name.
   */
  private static final String TIME_KEY = "observationTime";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, BatchId object) {
    MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(DATE_KEY, null, object.getObservationDate());
    msg.add(TIME_KEY, null, object.getObservationTime());
    return msg;
  }

  @Override
  public BatchId buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeField dateField = message.getByName(DATE_KEY);
    FudgeField timeField = message.getByName(TIME_KEY);

    Validate.notNull(dateField, "Fudge message is not a BatchId - field " + DATE_KEY + " is not present");
    Validate.notNull(timeField, "Fudge message is not a BatchId - field " + TIME_KEY + " is not present");

    LocalDate date = message.getFieldValue(LocalDate.class, dateField);
    String time = message.getFieldValue(String.class, timeField);
    
    return new BatchId(date, time);
  }

}
