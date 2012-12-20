/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.equity;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.id.ExternalIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code EquityVarianceSwapSecurity}.
 */
@FudgeBuilderFor(EquityVarianceSwapSecurity.class)
public class EquityVarianceSwapSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquityVarianceSwapSecurity> {

  /** Field name. */
  public static final String SPOT_UNDERLYING_IDENTIFIER_FIELD_NAME = "spotUnderlyingIdentifier";
  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String STRIKE_FIELD_NAME = "strike";
  /** Field name. */
  public static final String NOTIONAL_FIELD_NAME = "notional";
  /** Field name. */
  public static final String PARAMETERIZED_AS_VARIANCE_FIELD_NAME = "parameterizedAsVariance";
  /** Field name. */
  public static final String ANNUALIZATION_FACTOR_FIELD_NAME = "annualizationFactor";
  /** Field name. */
  public static final String FIRST_OBSERVATION_DATE_FIELD_NAME = "firstObservationDate";
  /** Field name. */
  public static final String LAST_OBSERVATION_DATE_FIELD_NAME = "lastObservationDate";
  /** Field name. */
  public static final String SETTLEMENT_DATE_FIELD_NAME = "settlementDate";
  /** Field name. */
  public static final String REGION_FIELD_NAME = "region";
  /** Field name. */
  public static final String OBSERVATION_FREQUENCY_FIELD_NAME = "observationFrequency";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EquityVarianceSwapSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquityVarianceSwapSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EquityVarianceSwapSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, SPOT_UNDERLYING_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getSpotUnderlyingId()));
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, STRIKE_FIELD_NAME, object.getStrike());
    addToMessage(msg, NOTIONAL_FIELD_NAME, object.getNotional());
    addToMessage(msg, PARAMETERIZED_AS_VARIANCE_FIELD_NAME, object.isParameterizedAsVariance());
    addToMessage(msg, ANNUALIZATION_FACTOR_FIELD_NAME, object.getAnnualizationFactor());
    addToMessage(msg, FIRST_OBSERVATION_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getFirstObservationDate()));
    addToMessage(msg, LAST_OBSERVATION_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getLastObservationDate()));
    addToMessage(msg, SETTLEMENT_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getSettlementDate()));
    addToMessage(msg, REGION_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, OBSERVATION_FREQUENCY_FIELD_NAME, object.getObservationFrequency());
  }

  @Override
  public EquityVarianceSwapSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EquityVarianceSwapSecurity object = new EquityVarianceSwapSecurity();
    EquityVarianceSwapSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EquityVarianceSwapSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setSpotUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SPOT_UNDERLYING_IDENTIFIER_FIELD_NAME)));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setStrike(msg.getDouble(STRIKE_FIELD_NAME));
    object.setNotional(msg.getDouble(NOTIONAL_FIELD_NAME));
    object.setParameterizedAsVariance(msg.getBoolean(PARAMETERIZED_AS_VARIANCE_FIELD_NAME));
    object.setAnnualizationFactor(msg.getDouble(ANNUALIZATION_FACTOR_FIELD_NAME));
    object.setFirstObservationDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FIRST_OBSERVATION_DATE_FIELD_NAME)));
    object.setLastObservationDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(LAST_OBSERVATION_DATE_FIELD_NAME)));
    object.setSettlementDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_DATE_FIELD_NAME)));
    object.setRegionId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_FIELD_NAME)));
    object.setObservationFrequency(msg.getValue(Frequency.class, OBSERVATION_FREQUENCY_FIELD_NAME));
  }

}
