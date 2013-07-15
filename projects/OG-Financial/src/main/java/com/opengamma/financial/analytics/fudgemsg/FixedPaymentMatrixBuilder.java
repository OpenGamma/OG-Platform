/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.financial.analytics.cashflow.FixedPaymentMatrix;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
@FudgeBuilderFor(FixedPaymentMatrix.class)
public class FixedPaymentMatrixBuilder extends AbstractFudgeBuilder<FixedPaymentMatrix> {
  private static final String DATES_FIELD = "dates";
  private static final String MCA_FIELD = "mca";
  private static final String MAX_AMOUNTS_FIELD = "maxAmounts";

  @Override
  public FixedPaymentMatrix buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final List<FudgeField> dateFields = message.getAllByName(DATES_FIELD);
    final List<FudgeField> mcaFields = message.getAllByName(MCA_FIELD);
    final Map<LocalDate, MultipleCurrencyAmount> values = Maps.newHashMapWithExpectedSize(dateFields.size());
    for (int i = 0; i < dateFields.size(); i++) {
      final LocalDate date = deserializer.fieldValueToObject(LocalDate.class, dateFields.get(i));
      final MultipleCurrencyAmount mca = deserializer.fieldValueToObject(MultipleCurrencyAmount.class, mcaFields.get(i));
      values.put(date, mca);
    }
    final int maxAmounts = message.getInt(MAX_AMOUNTS_FIELD);
    return new FixedPaymentMatrix(values, maxAmounts);
  }

  @Override
  protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final FixedPaymentMatrix object) {
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : object.getValues().entrySet()) {
      serializer.addToMessageWithClassHeaders(message, DATES_FIELD, null, entry.getKey());
      serializer.addToMessageWithClassHeaders(message, MCA_FIELD, null, entry.getValue());
    }
    message.add(MAX_AMOUNTS_FIELD, object.getMaxCurrencyAmounts());
  }

}
