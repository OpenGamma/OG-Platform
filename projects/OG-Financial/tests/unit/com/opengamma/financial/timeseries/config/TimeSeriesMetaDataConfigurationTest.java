/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.config;

import static com.opengamma.financial.timeseries.config.TimeSeriesMetaDataFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.financial.timeseries.config.TimeSeriesMetaDataFieldNames.DATA_SOURCE_NAME;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.opengamma.financial.timeseries.config.TimeSeriesMetaDataConfiguration;
import com.opengamma.financial.timeseries.config.TimeSeriesMetaDataRating;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * Test TimeSeriesMetaDataConfiguration to/from fudge message
 */
public class TimeSeriesMetaDataConfigurationTest {

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  
  @Test
  public void fudgeEncoding() {
    
    List<TimeSeriesMetaDataRating> rules = new ArrayList<TimeSeriesMetaDataRating>();
    rules.add(new TimeSeriesMetaDataRating(DATA_SOURCE_NAME, "BLOOMBERG", 2));
    rules.add(new TimeSeriesMetaDataRating(DATA_SOURCE_NAME, "REUTERS", 1));
    rules.add(new TimeSeriesMetaDataRating(DATA_PROVIDER_NAME, "CMPL", 3));
    
    TimeSeriesMetaDataConfiguration inputConfig = new TimeSeriesMetaDataConfiguration(rules);
    
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(s_fudgeContext);
    MutableFudgeFieldContainer inputMsg = serializationContext.objectToFudgeMsg(inputConfig);
    FudgeFieldContainer outputMsg = s_fudgeContext.deserialize(s_fudgeContext.toByteArray(inputMsg)).getMessage();
    assertNotNull(outputMsg);
    assertEquals(3, outputMsg.getNumFields());
        
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(s_fudgeContext);
    TimeSeriesMetaDataConfiguration outputConfig = deserializationContext.fudgeMsgToObject(TimeSeriesMetaDataConfiguration.class, outputMsg);
    
    assertEquals(inputConfig, outputConfig);
    
  }
}
