/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.livedata.permission.PermissionUtils;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test EID normalisation in liveData
 */
@Test
public class BloombergEidFieldValueNormalizerTest {

  public void normalizeEidNameAndValue() {
    BloombergEidFieldValueNormalizer normalizer = new BloombergEidFieldValueNormalizer();

    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(BloombergConstants.EID_LIVE_DATA_FIELD, 10);
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    msg.add(BloombergConstants.EID_LIVE_DATA_FIELD, 20);
    msg.add(BloombergConstants.EID_DATA.toString(), 30);
    msg.add(BloombergConstants.EID_DATA.toString(), 40);

    MutableFudgeMsg normalized = normalizer.apply(msg, "test", new FieldHistoryStore());
    assertEquals(6, normalized.getAllFields().size());
    List<FudgeField> eidLiveData = normalized.getAllByName(BloombergConstants.EID_LIVE_DATA_FIELD);
    assertTrue(eidLiveData.isEmpty());

    List<FudgeField> eidRefData = normalized.getAllByName(BloombergConstants.EID_DATA.toString());
    assertTrue(eidRefData.isEmpty());

    List<FudgeField> permissions = normalized.getAllByName(PermissionUtils.LIVE_DATA_PERMISSION_FIELD);
    assertEquals(4, permissions.size());
    List<String> permissionValues = Lists.newArrayList();
    for (FudgeField fudgeField : permissions) {
      permissionValues.add((String) fudgeField.getValue());
    }
    assertTrue(permissionValues.contains("Data:Bloomberg:EID:10"));
    assertTrue(permissionValues.contains("Data:Bloomberg:EID:20"));
    assertTrue(permissionValues.contains("Data:Bloomberg:EID:30"));
    assertTrue(permissionValues.contains("Data:Bloomberg:EID:40"));
  }
}
