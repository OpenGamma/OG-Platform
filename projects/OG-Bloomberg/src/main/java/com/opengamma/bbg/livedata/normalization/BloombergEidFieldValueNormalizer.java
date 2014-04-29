/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.normalization;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.google.common.collect.Lists;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergPermissions;
import com.opengamma.livedata.normalization.NormalizationRule;
import com.opengamma.livedata.permission.PermissionUtils;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class BloombergEidFieldValueNormalizer implements NormalizationRule {

  private static final String EID_LIVE_DATA = BloombergConstants.EID_LIVE_DATA_FIELD;
  private static final String EID_REF_DATA = BloombergConstants.EID_DATA.toString();

  private final FudgeDeserializer _fudgeDeserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());

  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    List<String> toRemove = Lists.newArrayList();
    List<FudgeField> eidLiveData = msg.getAllByName(EID_LIVE_DATA);
    for (FudgeField fudgeField : eidLiveData) {
      try {
        Integer eidValue = _fudgeDeserializer.fieldValueToObject(Integer.class, fudgeField);
        msg.add(PermissionUtils.LIVE_DATA_PERMISSION_FIELD, BloombergPermissions.createEidPermissionString((int) eidValue));
        toRemove.add(fudgeField.getName());
      } catch (Exception ex) {
        //ignore
      }
    }

    List<FudgeField> eidRefData = msg.getAllByName(EID_REF_DATA);
    for (FudgeField fudgeField : eidRefData) {
      try {
        Integer eidValue = _fudgeDeserializer.fieldValueToObject(Integer.class, fudgeField);
        msg.add(PermissionUtils.LIVE_DATA_PERMISSION_FIELD, BloombergPermissions.createEidPermissionString((int) eidValue));
        toRemove.add(fudgeField.getName());
      } catch (Exception ex) {
        //ignore
      }
    }

    for (String fieldName : toRemove) {
      msg.remove(fieldName);
    }
    return msg;
  }
}
