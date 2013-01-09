/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import static org.testng.AssertJUnit.assertEquals;

import org.joda.beans.JodaBeanUtils;
import org.joda.convert.StringConverter;
import org.testng.annotations.Test;

import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.SamplingFrequency;

/**
 *
 */
public class JodaBeanConvertersTest {

  static {
    // load and register converters
    JodaBeanConverters.getInstance();
  }

  @Test
  public void barrierTypes() {
    StringConverter<BarrierType> converter = JodaBeanUtils.stringConverter().findConverter(BarrierType.class);
    assertEquals("Up", converter.convertToString(BarrierType.UP));
    assertEquals("Down", converter.convertToString(BarrierType.DOWN));
    assertEquals("Double", converter.convertToString(BarrierType.DOUBLE));
    assertEquals(BarrierType.UP, converter.convertFromString(BarrierType.class, "Up"));
    assertEquals(BarrierType.DOWN, converter.convertFromString(BarrierType.class, "Down"));
    assertEquals(BarrierType.DOUBLE, converter.convertFromString(BarrierType.class, "Double"));
  }

  @Test
  public void barrierDirections() {
    StringConverter<BarrierDirection> converter = JodaBeanUtils.stringConverter().findConverter(BarrierDirection.class);
    assertEquals("Knock In", converter.convertToString(BarrierDirection.KNOCK_IN));
    assertEquals("Knock Out", converter.convertToString(BarrierDirection.KNOCK_OUT));
    assertEquals(BarrierDirection.KNOCK_IN, converter.convertFromString(BarrierDirection.class, "Knock In"));
    assertEquals(BarrierDirection.KNOCK_OUT, converter.convertFromString(BarrierDirection.class, "Knock Out"));
  }

  @Test
  public void samplingFrequencies() {
    StringConverter<SamplingFrequency> converter = JodaBeanUtils.stringConverter().findConverter(SamplingFrequency.class);
    assertEquals("Daily Close", converter.convertToString(SamplingFrequency.DAILY_CLOSE));
    assertEquals("Friday", converter.convertToString(SamplingFrequency.FRIDAY));
    assertEquals("Weekly Close", converter.convertToString(SamplingFrequency.WEEKLY_CLOSE));
    assertEquals("Continuous", converter.convertToString(SamplingFrequency.CONTINUOUS));
    assertEquals("One Look", converter.convertToString(SamplingFrequency.ONE_LOOK));
    assertEquals(SamplingFrequency.DAILY_CLOSE, converter.convertFromString(SamplingFrequency.class, "Daily Close"));
    assertEquals(SamplingFrequency.FRIDAY, converter.convertFromString(SamplingFrequency.class, "Friday"));
    assertEquals(SamplingFrequency.WEEKLY_CLOSE, converter.convertFromString(SamplingFrequency.class, "Weekly Close"));
    assertEquals(SamplingFrequency.CONTINUOUS, converter.convertFromString(SamplingFrequency.class, "Continuous"));
    assertEquals(SamplingFrequency.ONE_LOOK, converter.convertFromString(SamplingFrequency.class, "One Look"));
  }
}
