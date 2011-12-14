/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Tests the {@link SequentialDataNormalizer} class.
 */
@Test
public class CombinedDataNormalizerTest {

  private static class Normalizer implements HistoricalMarketDataNormalizer {

    private Object _outputValue;
    private Object _inputValue;

    public Object getInputValue() {
      return _inputValue;
    }

    public void setOutputValue(final Object outputValue) {
      _outputValue = outputValue;
    }

    @Override
    public Object normalize(ExternalIdBundle identifiers, String name, Object value) {
      assertEquals(identifiers, ExternalIdBundle.of(ExternalId.of("Foo", "Bar")));
      assertEquals(name, "Test");
      _inputValue = value;
      return _outputValue;
    }

  }

  public void testFirst() {
    final Normalizer a = new Normalizer();
    a.setOutputValue("A");
    final Normalizer b = new Normalizer();
    b.setOutputValue("B");
    final HistoricalMarketDataNormalizer normalizer = new SequentialDataNormalizer(Arrays.asList(a, b));
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("Foo", "Bar")), "Test", "X"), "A");
    assertEquals(a.getInputValue(), "X");
    assertEquals(b.getInputValue(), null);
  }

  public void testSecond() {
    final Normalizer a = new Normalizer();
    final Normalizer b = new Normalizer();
    b.setOutputValue("B");
    final HistoricalMarketDataNormalizer normalizer = new SequentialDataNormalizer(Arrays.asList(a, b));
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("Foo", "Bar")), "Test", "X"), "B");
    assertEquals(a.getInputValue(), "X");
    assertEquals(b.getInputValue(), "X");
  }

  public void testNone() {
    final Normalizer a = new Normalizer();
    final Normalizer b = new Normalizer();
    final HistoricalMarketDataNormalizer normalizer = new SequentialDataNormalizer(Arrays.asList(a, b));
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("Foo", "Bar")), "Test", "X"), null);
    assertEquals(a.getInputValue(), "X");
    assertEquals(b.getInputValue(), "X");
  }

}
