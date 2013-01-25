/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivities;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;

/**
 * A Fudge builder for {@code BondFutureSecurity}.
 */
@FudgeBuilderFor(SecurityEntryData.class)
public class SecurityEntryDataFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<SecurityEntryData> {

  /** Field name. */
  public static final String FACTOR_SET_EXTERNAL_ID_FIELD_NAME = "factSetExternalId";
  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  private static final String MATURITY_DATE_FIELD_NAME = "factorName";
  /** Field name. */
  private static final String EXTERNAL_ID_FIELD_NAME = "id";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SecurityEntryData object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, EXTERNAL_ID_FIELD_NAME, null, object.getId());
    serializer.addToMessage(msg, CURRENCY_FIELD_NAME, null, object.getCurrency());
    serializer.addToMessage(msg, MATURITY_DATE_FIELD_NAME, null, object.getMaturityDate());
    serializer.addToMessage(msg, FACTOR_SET_EXTERNAL_ID_FIELD_NAME, null, object.getFactorSetId());
    return msg;
  }

  @Override
  public SecurityEntryData buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    ExternalId id = ExternalId.parse(msg.getString(EXTERNAL_ID_FIELD_NAME));
    Currency currency = Currency.of(msg.getString(CURRENCY_FIELD_NAME));
    LocalDate maturityDate = msg.getValue(LocalDate.class, MATURITY_DATE_FIELD_NAME);
    ExternalId factorSetId = ExternalId.parse(msg.getString(FACTOR_SET_EXTERNAL_ID_FIELD_NAME));
    
    SecurityEntryData data = new SecurityEntryData(id, currency, maturityDate, factorSetId);
    return data;
  }

}
