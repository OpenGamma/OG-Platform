/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge.
 */
@Test(groups = TestGroup.UNIT)
public class ManageableRegionFudgeEncodingTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ManageableRegionFudgeEncodingTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void test() {
    ManageableRegion obj = new ManageableRegion();
    obj.setUniqueId(UniqueId.of("U", "1"));
    obj.setExternalIdBundle(ExternalIdBundle.of("A", "B"));
    obj.setClassification(RegionClassification.INDEPENDENT_STATE);
    obj.setParentRegionIds(ImmutableSet.of(UniqueId.of("U", "1"), UniqueId.of("U", "2")));
    obj.setName("Test");
    obj.setFullName("Testland");
    obj.getData().set("P1", "A");
    obj.getData().set("P2", "B");
    testFudgeMessage(obj);
  }

  private void testFudgeMessage(final ManageableRegion obj) {
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    FudgeMsg msg = serializer.objectToFudgeMsg(obj);
    s_logger.debug("ManageableRegion {}", obj);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to {}", msg);
    final ManageableRegion decoded = s_fudgeContext.fromFudgeMsg(ManageableRegion.class, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!obj.equals(decoded)) {
      s_logger.warn("Expected {}", obj);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
