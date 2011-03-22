/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertNull;
import org.testng.annotations.Test;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * 
 */
public class NormalizationRuleSetTest {

  /**
   * First filter will remove the message entirely.
   * Testing to make sure that the break condition happens, and that
   * no NPEs happen.
   */
  @Test
  public void filterRemovingMessageEntirely() {
    NormalizationRuleSet ruleSet = new NormalizationRuleSet(
        "Testing",
        new RequiredFieldFilter("Foo"),
        new FieldFilter("Bar"));
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    FudgeFieldContainer normalizedMsg = ruleSet.getNormalizedMessage(msg, new FieldHistoryStore());
    assertNull(normalizedMsg);
  }
}
