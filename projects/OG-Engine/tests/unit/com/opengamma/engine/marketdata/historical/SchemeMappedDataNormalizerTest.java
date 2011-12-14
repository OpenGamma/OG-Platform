/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the {@link SchemeMappedDataNormalizer} class.
 */
@Test
public class SchemeMappedDataNormalizerTest {

  private static class Normalizer implements HistoricalMarketDataNormalizer {

    private boolean _used;

    public boolean isUsed() {
      return _used;
    }

    @Override
    public Object normalize(ExternalIdBundle identifiers, String name, Object value) {
      _used = true;
      return value;
    }

    @Override
    public Map<Pair<ExternalIdBundle, String>, Object> normalize(final Map<Pair<ExternalIdBundle, String>, Object> values) {
      _used = true;
      return values;
    }

  }

  private Object single(final HistoricalMarketDataNormalizer normalizer, final ExternalIdBundle identifiers) {
    return normalizer.normalize(identifiers, "Test", "X");
  }

  private Object multiple(final HistoricalMarketDataNormalizer normalizer, final ExternalIdBundle identifiers) {
    final Map<Pair<ExternalIdBundle, String>, Object> request = Collections.<Pair<ExternalIdBundle, String>, Object>singletonMap(Pair.of(identifiers, "Test"), "X");
    final Map<Pair<ExternalIdBundle, String>, Object> response = normalizer.normalize(request);
    assertNotNull(response);
    return response.get(Pair.of(identifiers, "Test"));
  }

  public void testStrings() {
    final Normalizer a = new Normalizer();
    final Normalizer b = new Normalizer();
    final Map<Object, HistoricalMarketDataNormalizer> normalizers = new HashMap<Object, HistoricalMarketDataNormalizer>();
    normalizers.put("A", a);
    normalizers.put("B", b);
    final HistoricalMarketDataNormalizer normalizer = new SchemeMappedDataNormalizer(normalizers);
    assertEquals(single(normalizer, ExternalIdBundle.of(ExternalId.of("A", "Foo"))), "X");
    assertEquals(multiple(normalizer, ExternalIdBundle.of(ExternalId.of("A", "Foo"))), "X");
    assertTrue(a.isUsed());
    assertFalse(b.isUsed());
  }

  public void testExternalSchemes() {
    final Normalizer a = new Normalizer();
    final Normalizer b = new Normalizer();
    final Map<Object, HistoricalMarketDataNormalizer> normalizers = new HashMap<Object, HistoricalMarketDataNormalizer>();
    normalizers.put(ExternalScheme.of("A"), a);
    normalizers.put(ExternalScheme.of("B"), b);
    final HistoricalMarketDataNormalizer normalizer = new SchemeMappedDataNormalizer(normalizers);
    assertEquals(single(normalizer, ExternalIdBundle.of(ExternalId.of("B", "Foo"))), "X");
    assertEquals(multiple(normalizer, ExternalIdBundle.of(ExternalId.of("B", "Foo"))), "X");
    assertTrue(b.isUsed());
    assertFalse(a.isUsed());
  }

  public void testCollection() {
    final Normalizer a = new Normalizer();
    final Normalizer b = new Normalizer();
    final Map<Object, HistoricalMarketDataNormalizer> normalizers = new HashMap<Object, HistoricalMarketDataNormalizer>();
    normalizers.put(Arrays.asList("A1", "A2", "A3"), a);
    normalizers.put(Arrays.asList("B1", "B2", "B3"), b);
    final HistoricalMarketDataNormalizer normalizer = new SchemeMappedDataNormalizer(normalizers);
    assertEquals(single(normalizer, ExternalIdBundle.of(ExternalId.of("A", "Foo"), ExternalId.of("B2", "Foo"))), "X");
    assertEquals(multiple(normalizer, ExternalIdBundle.of(ExternalId.of("A", "Foo"), ExternalId.of("B2", "Foo"))), "X");
    assertTrue(b.isUsed());
    assertFalse(a.isUsed());
  }

}
