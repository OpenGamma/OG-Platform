/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.PaymentScheduleMatrix;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
@FudgeBuilderFor(PaymentScheduleMatrix.class)
public class PaymentScheduleMatrixBuilder implements FudgeBuilder<PaymentScheduleMatrix> {
  private static final String DATES_FIELD = "dates";
  private static final String MCA_FIELD = "mca";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final PaymentScheduleMatrix object) {
    final MutableFudgeMsg message = serializer.newMessage();
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : object.getValues().entrySet()) {
      serializer.addToMessageWithClassHeaders(message, DATES_FIELD, null, entry.getKey());
      serializer.addToMessageWithClassHeaders(message, MCA_FIELD, null, entry.getValue());
    }
    return message;
  }

  @Override
  public PaymentScheduleMatrix buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Map<LocalDate, MultipleCurrencyAmount> values = new HashMap<LocalDate, MultipleCurrencyAmount>();
    final List<FudgeField> dateFields = message.getAllByName(DATES_FIELD);
    final List<FudgeField> mcaFields = message.getAllByName(MCA_FIELD);
    for (int i = 0; i < dateFields.size(); i++) {
      final LocalDate date = deserializer.fieldValueToObject(LocalDate.class, dateFields.get(i));
      final MultipleCurrencyAmount mca = deserializer.fieldValueToObject(MultipleCurrencyAmount.class, mcaFields.get(i));
      values.put(date, mca);
    }
    return new PaymentScheduleMatrix(values);
  }

}
