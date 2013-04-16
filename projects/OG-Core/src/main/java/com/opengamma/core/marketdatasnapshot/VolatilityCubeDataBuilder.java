/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Fudge builder for {@code VolatilityCubeData}.
 */
@FudgeBuilderFor(VolatilityCubeData.class)
public class VolatilityCubeDataBuilder implements FudgeBuilder<VolatilityCubeData> {

  /** Field name. */
  public static final String DATA_POINTS_FIELD_NAME = "dataPoints";
  /** Field name. */
  public static final String DATA_IDS_FIELD_NAME = "dataIds";
  /** Field name. */
  public static final String DATA_RELATIVE_STRIKES_FIELD_NAME = "dataRelativeStrikes";
  /** Field name. */
  public static final String OTHER_DATA_FIELD_NAME = "otherData";
  /** Field name. */
  public static final String STRIKES_FIELD_NAME = "strikes";
  /** Field name. */
  public static final String ATM_VOLS_FIELD_NAME = "ATM volatilities";
  /** Field name. */
  public static final String SWAP_TENOR_FIELD_NAME = "swapTenor";
  /** Field name. */
  public static final String OPTION_EXPIRY_FIELD_NAME = "optionExpiry";
  /** Field name. */
  public static final String STRIKE_FIELD_NAME = "strike";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilityCubeData object) {
    final MutableFudgeMsg ret = serializer.newMessage();
    FudgeSerializer.addClassHeader(ret, VolatilityCubeData.class);

    serializer.addToMessage(ret, DATA_POINTS_FIELD_NAME, null, object.getDataPoints());
    serializer.addToMessage(ret, OTHER_DATA_FIELD_NAME, null, object.getOtherData());
    if (object.getDataIds() != null) {
      serializer.addToMessage(ret, DATA_IDS_FIELD_NAME, null, object.getDataIds());
    }
    if (object.getRelativeStrikes() != null) {
      serializer.addToMessage(ret, DATA_RELATIVE_STRIKES_FIELD_NAME, null, object.getRelativeStrikes());
    }

    if (object.getATMStrikes() != null) {
      final MutableFudgeMsg strikesMessage = serializer.newMessage();
      for (final Entry<Pair<Tenor, Tenor>, Double> entry : object.getATMStrikes().entrySet()) {
        final MutableFudgeMsg strikeMessage = serializer.newMessage();
        serializer.addToMessage(strikeMessage, SWAP_TENOR_FIELD_NAME, null, entry.getKey().getFirst());
        serializer.addToMessage(strikeMessage, OPTION_EXPIRY_FIELD_NAME, null, entry.getKey().getSecond());
        serializer.addToMessage(strikeMessage, STRIKE_FIELD_NAME, null, entry.getValue());

        strikesMessage.add(null, null, strikeMessage);
      }
      ret.add(STRIKES_FIELD_NAME, strikesMessage);
    }
    if (object.getATMVolatilities() != null) {
      final MutableFudgeMsg atmVolsMessage = serializer.newMessage();
      for (final Entry<Pair<Tenor, Tenor>, Double> entry : object.getATMVolatilities().entrySet()) {
        final MutableFudgeMsg atmVolMessage = serializer.newMessage();
        serializer.addToMessage(atmVolMessage, SWAP_TENOR_FIELD_NAME, null, entry.getKey().getFirst());
        serializer.addToMessage(atmVolMessage, OPTION_EXPIRY_FIELD_NAME, null, entry.getKey().getSecond());
        serializer.addToMessage(atmVolMessage, ATM_VOLS_FIELD_NAME, null, entry.getValue());

        atmVolsMessage.add(null, null, atmVolMessage);
      }
      ret.add(ATM_VOLS_FIELD_NAME, atmVolsMessage);
    }
    return ret;
  }

  @Override
  public VolatilityCubeData buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final Class<?> mapClass = Map.class;
    final FudgeField pointsField = message.getByName(DATA_POINTS_FIELD_NAME);
    final FudgeField idsField = message.getByName(DATA_IDS_FIELD_NAME);
    final FudgeField relativeStrikesField = message.getByName(DATA_RELATIVE_STRIKES_FIELD_NAME);

    @SuppressWarnings("unchecked")
    final Map<VolatilityPoint, Double> dataPoints = (Map<VolatilityPoint, Double>) (pointsField == null ? null : deserializer.fieldValueToObject(mapClass, pointsField));
    @SuppressWarnings("unchecked")
    final Map<VolatilityPoint, ExternalIdBundle> dataIds = (Map<VolatilityPoint, ExternalIdBundle>) (idsField == null ? null : deserializer.fieldValueToObject(mapClass, idsField));
    @SuppressWarnings("unchecked")
    final Map<VolatilityPoint, Double> relativeStrikes = (Map<VolatilityPoint, Double>) (relativeStrikesField == null ? null : deserializer.fieldValueToObject(mapClass, relativeStrikesField));

    final FudgeField otherField = message.getByName(OTHER_DATA_FIELD_NAME);
    final SnapshotDataBundle otherData = otherField == null ? null : deserializer.fieldValueToObject(SnapshotDataBundle.class, otherField);

    final VolatilityCubeData ret = new VolatilityCubeData();
    ret.setDataPoints(dataPoints);
    ret.setDataIds(dataIds);
    ret.setRelativeStrikes(relativeStrikes);
    ret.setOtherData(otherData);

    final FudgeMsg strikesMsg = message.getMessage(STRIKES_FIELD_NAME);

    if (strikesMsg != null) {
      final Map<Pair<Tenor, Tenor>, Double> strikes = Maps.newHashMap();
      for (final FudgeField strikeField : strikesMsg) {
        final FudgeMsg strikeMsg = (FudgeMsg) strikeField.getValue();
        final Tenor swapTenor = deserializer.fieldValueToObject(Tenor.class, strikeMsg.getByName(SWAP_TENOR_FIELD_NAME));
        final Tenor optionExpiry = deserializer.fieldValueToObject(Tenor.class, strikeMsg.getByName(OPTION_EXPIRY_FIELD_NAME));
        final Double strike = deserializer.fieldValueToObject(Double.class, strikeMsg.getByName(STRIKE_FIELD_NAME));
        strikes.put(Pair.of(swapTenor, optionExpiry), strike);
      }
      ret.setATMStrikes(strikes);
    }

    final FudgeMsg atmVolatilitiesMsg = message.getMessage(ATM_VOLS_FIELD_NAME);
    if (atmVolatilitiesMsg != null) {
      final Map<Pair<Tenor, Tenor>, Double> atmVols = Maps.newHashMap();
      for (final FudgeField atmVolField : atmVolatilitiesMsg) {
        final FudgeMsg atmVolatilityMsg = (FudgeMsg) atmVolField.getValue();
        final Tenor swapTenor = deserializer.fieldValueToObject(Tenor.class, atmVolatilityMsg.getByName(SWAP_TENOR_FIELD_NAME));
        final Tenor optionExpiry = deserializer.fieldValueToObject(Tenor.class, atmVolatilityMsg.getByName(OPTION_EXPIRY_FIELD_NAME));
        final Double atmVol = deserializer.fieldValueToObject(Double.class, atmVolatilityMsg.getByName(ATM_VOLS_FIELD_NAME));
        atmVols.put(Pair.of(swapTenor, optionExpiry), atmVol);
      }
      ret.setATMVolatilities(atmVols);
    }
    return ret;
  }

}
