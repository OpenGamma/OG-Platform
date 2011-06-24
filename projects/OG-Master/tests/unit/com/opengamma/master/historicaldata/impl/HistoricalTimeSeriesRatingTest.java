/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import static com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.testng.annotations.Test;

import com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.historicaldata.impl.HistoricalTimeSeriesRatingRule;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Test HistoricalTimeSeriesRating to/from fudge message.
 */
@Test
public class HistoricalTimeSeriesRatingTest {

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  public void fudgeEncoding() {
    List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<HistoricalTimeSeriesRatingRule>();
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, "BLOOMBERG", 2));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, "REUTERS", 1));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, "CMPL", 3));
    
    HistoricalTimeSeriesRating inputConfig = new HistoricalTimeSeriesRating(rules);
    
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(s_fudgeContext);
    MutableFudgeMsg inputMsg = serializationContext.objectToFudgeMsg(inputConfig);
    FudgeMsg outputMsg = s_fudgeContext.deserialize(s_fudgeContext.toByteArray(inputMsg)).getMessage();
    assertNotNull(outputMsg);
    assertEquals(3, outputMsg.getNumFields());
    
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(s_fudgeContext);
    HistoricalTimeSeriesRating outputConfig = deserializationContext.fudgeMsgToObject(HistoricalTimeSeriesRating.class, outputMsg);
    
    assertEquals(inputConfig, outputConfig);
  }

}
