/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Period;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.util.time.Tenor;

/**
 * Fudge builder for {@link CurveNodeIdMapper}
 */
@FudgeBuilderFor(CurveNodeIdMapper.class)
public class CurveNodeIdMapperBuilder implements FudgeBuilder<CurveNodeIdMapper> {
  /** The name field */
  private static final String NAME_FIELD = "name";
  /** The cash ids field */
  private static final String CASH_NODE_FIELD = "cashIds";
  /** The continuously compounded node field */
  private static final String CONTINUOUSLY_COMPOUNDED_NODE_FIELD = "continuouslyCompoundedIds";
  /** The credit spread node field */
  private static final String CREDIT_SPREAD_NODE_FIELD = "creditSpreadIds";
  /** The discount factor node field */
  private static final String DISCOUNT_FACTOR_NODE_FIELD = "discountFactorIds";
  /** The FRA node field */
  private static final String FRA_NODE_FIELD = "fraIds";
  /** The FX forward node field */
  private static final String FX_FORWARD_NODE_FIELD = "fxForwardIds";
  /** The rate future node field */
  private static final String RATE_FUTURE_FIELD = "rateFutureIds";
  /** The swap node field */
  private static final String SWAP_NODE_FIELD = "swapIds";
  /** The zero coupon inflation node field */
  private static final String ZERO_COUPON_INFLATION_NODE_FIELD = "zeroCouponInflationIds";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveNodeIdMapper object) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(null, 0, object.getClass().getName());
    message.add(NAME_FIELD, object.getName());
    if (object.getCashNodeIds() != null) {
      message.add(CASH_NODE_FIELD, getMessageForField(serializer, object.getCashNodeIds()));
    }
    if (object.getContinuouslyCompoundedRateNodeIds() != null) {
      message.add(CONTINUOUSLY_COMPOUNDED_NODE_FIELD, getMessageForField(serializer, object.getContinuouslyCompoundedRateNodeIds()));
    }
    if (object.getCreditSpreadNodeIds() != null) {
      message.add(CREDIT_SPREAD_NODE_FIELD, getMessageForField(serializer, object.getCreditSpreadNodeIds()));
    }
    if (object.getDiscountFactorNodeIds() != null) {
      message.add(DISCOUNT_FACTOR_NODE_FIELD, getMessageForField(serializer, object.getDiscountFactorNodeIds()));
    }
    if (object.getFRANodeIds() != null) {
      message.add(FRA_NODE_FIELD, getMessageForField(serializer, object.getFRANodeIds()));
    }
    if (object.getFXForwardNodeIds() != null) {
      message.add(FX_FORWARD_NODE_FIELD, getMessageForField(serializer, object.getFXForwardNodeIds()));
    }
    if (object.getRateFutureNodeIds() != null) {
      message.add(RATE_FUTURE_FIELD, getMessageForField(serializer, object.getRateFutureNodeIds()));
    }
    if (object.getSwapNodeIds() != null) {
      message.add(SWAP_NODE_FIELD, getMessageForField(serializer, object.getSwapNodeIds()));
    }
    if (object.getZeroCouponInflationNodeIds() != null) {
      message.add(ZERO_COUPON_INFLATION_NODE_FIELD, getMessageForField(serializer, object.getZeroCouponInflationNodeIds()));
    }
    return message;
  }

  @Override
  public CurveNodeIdMapper buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String name;
    if (message.hasField(NAME_FIELD)) {
      name = message.getString(NAME_FIELD);
    } else {
      name = null;
    }
    final Map<Tenor, CurveInstrumentProvider> cashNodeIds = getMapForField(CASH_NODE_FIELD, deserializer, message);
    final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateNodeIds = getMapForField(CONTINUOUSLY_COMPOUNDED_NODE_FIELD, deserializer, message);
    final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds = getMapForField(CREDIT_SPREAD_NODE_FIELD, deserializer, message);
    final Map<Tenor, CurveInstrumentProvider> discountFactorNodeIds = getMapForField(DISCOUNT_FACTOR_NODE_FIELD, deserializer, message);
    final Map<Tenor, CurveInstrumentProvider> fraNodeIds = getMapForField(FRA_NODE_FIELD, deserializer, message);
    final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds = getMapForField(FX_FORWARD_NODE_FIELD, deserializer, message);
    final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds = getMapForField(RATE_FUTURE_FIELD, deserializer, message);
    final Map<Tenor, CurveInstrumentProvider> swapNodeIds = getMapForField(SWAP_NODE_FIELD, deserializer, message);
    final Map<Tenor, CurveInstrumentProvider> zeroCouponInflationNodeIds = getMapForField(ZERO_COUPON_INFLATION_NODE_FIELD, deserializer, message);
    return new CurveNodeIdMapper(name, cashNodeIds, continuouslyCompoundedRateNodeIds, creditSpreadNodeIds, discountFactorNodeIds, fraNodeIds,
        fxForwardNodeIds, rateFutureNodeIds, swapNodeIds, zeroCouponInflationNodeIds);
  }

  private FudgeMsg getMessageForField(final FudgeSerializer serializer, final Map<Tenor, CurveInstrumentProvider> idMap) {
    final MutableFudgeMsg idsMessage = serializer.newMessage();
    for (final Map.Entry<Tenor, CurveInstrumentProvider> entry : idMap.entrySet()) {
      serializer.addToMessageWithClassHeaders(idsMessage, entry.getKey().getPeriod().toString(), null, entry.getValue(), CurveInstrumentProvider.class);
    }
    return idsMessage;
  }

  private Map<Tenor, CurveInstrumentProvider> getMapForField(final String fieldName, final FudgeDeserializer deserializer, final FudgeMsg message) {
    if (message.hasField(fieldName)) {
      final Map<Tenor, CurveInstrumentProvider> nodeIds = new HashMap<>();
      final FudgeMsg idsMessage = message.getMessage(fieldName);
      for (final FudgeField field : idsMessage.getAllFields()) {
        nodeIds.put(Tenor.of(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
      return nodeIds;
    }
    return null;
  }
}
