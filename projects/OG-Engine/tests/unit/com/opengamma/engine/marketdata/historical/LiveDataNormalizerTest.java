/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.normalization.DefaultNormalizer;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.StandardRuleResolver;
import com.opengamma.livedata.normalization.UnitChange;
import com.opengamma.livedata.resolver.IdResolver;

/**
 * Tests the {@link LiveDataNormalizer} class.
 */
@Test
public class LiveDataNormalizerTest {

  private static class Resolver implements IdResolver {

    @Override
    public ExternalId resolve(ExternalIdBundle ids) {
      final String v = ids.getValue(ExternalScheme.of("Scheme"));
      if (v != null) {
        return ExternalId.of("Unique", v);
      } else {
        return null;
      }
    }

    @Override
    public Map<ExternalIdBundle, ExternalId> resolve(Collection<ExternalIdBundle> ids) {
      fail();
      return null;
    }

  }

  public void testRuleSet () {
    final UnitChange rule = new UnitChange ("Foo", 0.01);
    final NormalizationRuleSet rules = new NormalizationRuleSet ("Test", rule);
    final LiveDataNormalizer normalizer = new LiveDataNormalizer(FudgeContext.GLOBAL_DEFAULT, new DefaultNormalizer(new Resolver(), new StandardRuleResolver(Arrays.asList(rules))), "Test");
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("Scheme", "A")), "Foo", 42d), 42d * 0.01);
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("Scheme", "A")), "Bar", 42d), 42d);
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("Test", "A")), "Foo", 42d), null);
    assertEquals(normalizer.normalize(ExternalIdBundle.of(ExternalId.of("Test", "A")), "Bar", 42d), null);
  }

}
