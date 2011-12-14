/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;

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

  }

  public void testStrings() {
    final Normalizer a = new Normalizer();
    final Normalizer b = new Normalizer();
    final Map<Object, HistoricalMarketDataNormalizer> normalizers = new HashMap<Object, HistoricalMarketDataNormalizer>();
    normalizers.put("A", a);
    normalizers.put("B", b);
    final HistoricalMarketDataNormalizer normalizer = new SchemeMappedDataNormalizer(normalizers);
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("A", "Foo")), "Test", "X"), "X");
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
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("B", "Foo")), "Test", "X"), "X");
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
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("A", "Foo"), ExternalId.of("B2", "Foo")), "Test", "X"), "X");
    assertTrue(b.isUsed());
    assertFalse(a.isUsed());
  }

}
