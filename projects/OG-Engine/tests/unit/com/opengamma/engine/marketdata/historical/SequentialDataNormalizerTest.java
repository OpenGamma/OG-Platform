/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the {@link SequentialDataNormalizer} class.
 */
@Test
public class SequentialDataNormalizerTest {

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

    @Override
    public Map<Pair<ExternalIdBundle, String>, Object> normalize(Map<Pair<ExternalIdBundle, String>, Object> values) {
      assertEquals(values.size(), 1);
      final Map.Entry<Pair<ExternalIdBundle, String>, Object> value = values.entrySet().iterator().next();
      final Object normalized = normalize(value.getKey().getFirst(), value.getKey().getSecond(), value.getValue());
      if (normalized == null) {
        return Collections.emptyMap();
      } else {
        return Collections.singletonMap(value.getKey(), normalized);
      }
    }

  }

  private Object single(final HistoricalMarketDataNormalizer normalizer) {
    return normalizer.normalize(ExternalIdBundle.of(ExternalId.of("Foo", "Bar")), "Test", "X");
  }

  private Object multiple(final HistoricalMarketDataNormalizer normalizer) {
    final Map<Pair<ExternalIdBundle, String>, Object> request = Collections
        .<Pair<ExternalIdBundle, String>, Object>singletonMap(Pair.of(ExternalIdBundle.of(ExternalId.of("Foo", "Bar")), "Test"), "X");
    final Map<Pair<ExternalIdBundle, String>, Object> response = normalizer.normalize(request);
    assertNotNull(response);
    return response.get(Pair.of(ExternalIdBundle.of(ExternalId.of("Foo", "Bar")), "Test"));
  }

  public void testFirst() {
    final Normalizer a = new Normalizer();
    a.setOutputValue("A");
    final Normalizer b = new Normalizer();
    b.setOutputValue("B");
    final HistoricalMarketDataNormalizer normalizer = new SequentialDataNormalizer(Arrays.asList(a, b));
    assertEquals(single(normalizer), "A");
    assertEquals(multiple(normalizer), "A");
    assertEquals(a.getInputValue(), "X");
    assertEquals(b.getInputValue(), null);
  }

  public void testSecond() {
    final Normalizer a = new Normalizer();
    final Normalizer b = new Normalizer();
    b.setOutputValue("B");
    final HistoricalMarketDataNormalizer normalizer = new SequentialDataNormalizer(Arrays.asList(a, b));
    assertEquals(single(normalizer), "B");
    assertEquals(multiple(normalizer), "B");
    assertEquals(a.getInputValue(), "X");
    assertEquals(b.getInputValue(), "X");
  }

  public void testNone() {
    final Normalizer a = new Normalizer();
    final Normalizer b = new Normalizer();
    final HistoricalMarketDataNormalizer normalizer = new SequentialDataNormalizer(Arrays.asList(a, b));
    assertEquals(single(normalizer), null);
    assertEquals(multiple(normalizer), null);
    assertEquals(a.getInputValue(), "X");
    assertEquals(b.getInputValue(), "X");
  }

}
