/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;
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
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, VolatilityCubeData object) {
    MutableFudgeMsg ret = serializer.newMessage();
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
      MutableFudgeMsg strikesMessage = serializer.newMessage();
      for (Entry<Pair<Tenor, Tenor>, Double> entry : object.getATMStrikes().entrySet()) {
        MutableFudgeMsg strikeMessage = serializer.newMessage();
        serializer.addToMessage(strikeMessage, SWAP_TENOR_FIELD_NAME, null, entry.getKey().getFirst());
        serializer.addToMessage(strikeMessage, OPTION_EXPIRY_FIELD_NAME, null, entry.getKey().getSecond());
        serializer.addToMessage(strikeMessage, STRIKE_FIELD_NAME, null, entry.getValue());        
        
        strikesMessage.add(null, null, strikeMessage);
      }
      ret.add(STRIKES_FIELD_NAME, strikesMessage);
    }
    if (object.getATMVolatilities() != null) {
      MutableFudgeMsg atmVolsMessage = serializer.newMessage();
      for (Entry<Pair<Tenor, Tenor>, Double> entry : object.getATMVolatilities().entrySet()) {
        MutableFudgeMsg atmVolMessage = serializer.newMessage();
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
  public VolatilityCubeData buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    Class<?> mapClass = (Class<?>) Map.class;
    FudgeField pointsField = message.getByName(DATA_POINTS_FIELD_NAME);
    FudgeField idsField = message.getByName(DATA_IDS_FIELD_NAME);
    FudgeField relativeStrikesField = message.getByName(DATA_RELATIVE_STRIKES_FIELD_NAME);
    
    @SuppressWarnings("unchecked")
    Map<VolatilityPoint, Double> dataPoints = (Map<VolatilityPoint, Double>) (pointsField == null ? null : deserializer.fieldValueToObject(mapClass, pointsField));
    @SuppressWarnings("unchecked")
    Map<VolatilityPoint, ExternalId> dataIds = (Map<VolatilityPoint, ExternalId>) (idsField == null ? null : deserializer.fieldValueToObject(mapClass, idsField));
    @SuppressWarnings("unchecked")
    Map<VolatilityPoint, Double> relativeStrikes = (Map<VolatilityPoint, Double>) (relativeStrikesField == null ? null : deserializer.fieldValueToObject(mapClass, relativeStrikesField));
    
    FudgeField otherField = message.getByName(OTHER_DATA_FIELD_NAME);
    SnapshotDataBundle otherData = otherField == null ? null : deserializer.fieldValueToObject(SnapshotDataBundle.class, otherField);
    
    VolatilityCubeData ret = new VolatilityCubeData();
    ret.setDataPoints(dataPoints);
    ret.setDataIds(dataIds);
    ret.setRelativeStrikes(relativeStrikes);
    ret.setOtherData(otherData);
    
    FudgeMsg strikesMsg = message.getMessage(STRIKES_FIELD_NAME);
    
    if (strikesMsg != null) {
      Map<Pair<Tenor, Tenor>, Double> strikes = new HashMap<Pair<Tenor, Tenor>, Double>();      
      for (FudgeField strikeField : strikesMsg) {
        FudgeMsg strikeMsg = (FudgeMsg) strikeField.getValue();
        Tenor swapTenor = deserializer.fieldValueToObject(Tenor.class, strikeMsg.getByName(SWAP_TENOR_FIELD_NAME));
        Tenor optionExpiry = deserializer.fieldValueToObject(Tenor.class, strikeMsg.getByName(OPTION_EXPIRY_FIELD_NAME));
        Double strike = deserializer.fieldValueToObject(Double.class, strikeMsg.getByName(STRIKE_FIELD_NAME));
        strikes.put(Pair.of(swapTenor, optionExpiry), strike);
      }
      ret.setATMStrikes(strikes);
    }
    
    FudgeMsg atmVolatilitiesMsg = message.getMessage(ATM_VOLS_FIELD_NAME);
    if (atmVolatilitiesMsg != null) {
      Map<Pair<Tenor, Tenor>, Double> atmVols = new HashMap<Pair<Tenor, Tenor>, Double>();      
      for (FudgeField atmVolField : atmVolatilitiesMsg) {
        FudgeMsg atmVolatilityMsg = (FudgeMsg) atmVolField.getValue();
        Tenor swapTenor = deserializer.fieldValueToObject(Tenor.class, atmVolatilityMsg.getByName(SWAP_TENOR_FIELD_NAME));
        Tenor optionExpiry = deserializer.fieldValueToObject(Tenor.class, atmVolatilityMsg.getByName(OPTION_EXPIRY_FIELD_NAME));
        Double atmVol = deserializer.fieldValueToObject(Double.class, atmVolatilityMsg.getByName(ATM_VOLS_FIELD_NAME));
        atmVols.put(Pair.of(swapTenor, optionExpiry), atmVol);
      }
      ret.setATMVolatilities(atmVols);
    }    
    return ret;
  }

}
