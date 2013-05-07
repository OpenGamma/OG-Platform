/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;
import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Given a Fudge message of a particular form, determine the set of Unique IDs
 * that correspond to a particular version correction.
 */
public final class FudgeUniqueIdResolutionTools {
  private FudgeUniqueIdResolutionTools() {
  }
  
  public static void appendNewVersion(
      MutableFudgeMsg message,
      FudgeContext fudgeContext,
      UniqueId newVersion,
      Instant effectiveInstantFrom,
      Instant effectiveInstantTo,
      Instant correctionInstantFrom,
      Instant correctionInstantTo) {
    
    // First, modify the previous version if required.
    // Can be changed to be index based for performance if necessary.
    List<FudgeField> fields = message.getAllFields();
    fields = Lists.reverse(fields);
    boolean foundMatchingOid = false;
    boolean mutatedOlderOid = false;
    for (FudgeField field : fields) {
      MutableFudgeMsg record = (MutableFudgeMsg) field.getValue();
      UniqueId recordId = UniqueId.parse(record.getString("uid"));
      if (!recordId.getObjectId().equals(newVersion.getObjectId())) {
        // No need to modify.
        continue;
      }
      
      foundMatchingOid = true;
      Instant recordEffectiveFrom = record.getFieldValue(Instant.class, record.getByName("eff-from"));
      Instant recordEffectiveTo = record.getFieldValue(Instant.class, record.getByName("eff-to"));
      //Instant recordCorrectionFrom = record.getFieldValue(Instant.class, record.getByName("cor-from"));
      Instant recordCorrectionTo = record.getFieldValue(Instant.class, record.getByName("cor-to"));
      
      if (ObjectUtils.equals(recordEffectiveFrom, effectiveInstantFrom)) {
        // The two EffectiveTo better match as well.
        ArgumentChecker.isTrue(ObjectUtils.equals(recordEffectiveTo, effectiveInstantTo), "Record matches effectiveFrom but not effectiveTo.");
        
        // Must be a new version on the correction.
        if (recordCorrectionTo != null) {
          continue;
        }
        record.remove("cor-to");
        record.add("cor-to", correctionInstantFrom);
        mutatedOlderOid = true;
      } else if (recordEffectiveFrom.isBefore(effectiveInstantFrom) && (recordEffectiveTo == null)) {
        record.add("eff-to", effectiveInstantFrom);
        mutatedOlderOid = true;
      }
      
    }
    if (foundMatchingOid && !mutatedOlderOid) {
      throw new OpenGammaRuntimeException("Algorithm failure: found existing OID but did not mutate any.");
    }
    
    MutableFudgeMsg record = fudgeContext.newMessage();
    record.add("uid", newVersion.toString());
    record.add("eff-from", effectiveInstantFrom);
    if (effectiveInstantTo != null) {
      record.add("eff-to", effectiveInstantTo);
    }
    record.add("corr-from", correctionInstantFrom);
    if (effectiveInstantTo != null) {
      record.add("corr-to", correctionInstantTo);
    }
    message.add(0, record);
  }

}
