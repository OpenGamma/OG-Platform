/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.Primitive;
import com.opengamma.engine.target.logger.ResolutionLogger;
import com.opengamma.engine.target.resolver.DeepResolver;
import com.opengamma.engine.target.resolver.ObjectResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Unit test for the {@link TargetResolutionLogger} class.
 */
@Test(groups = TestGroup.UNIT)
public class TargetResolutionLoggerTest {

  @SuppressWarnings("unchecked")
  public void testResolve_shallow() {
    final ComputationTargetResolver.AtVersionCorrection underlying = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
    final Set<UniqueId> expiredResolutions = new HashSet<UniqueId>();
    final ComputationTargetResolver.AtVersionCorrection resolver = TargetResolutionLogger.of(underlying, resolutions, expiredResolutions);
    final ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Foo", "Bar"));
    final ComputationTarget target = new ComputationTarget(spec.replaceIdentifier(UniqueId.of("Foo", "Bar", "Cow")), new Primitive(UniqueId.of("Foo", "Bar", "Cow")));
    Mockito.when(underlying.resolve(spec)).thenReturn(target);
    final ObjectResolver shallowResolver = Mockito.mock(ObjectResolver.class);
    Mockito.when(underlying.getResolver(spec)).thenReturn(shallowResolver);
    assertSame(resolver.resolve(spec), target);
  }

  @SuppressWarnings("unchecked")
  public void testResolve_deep() {
    final ComputationTargetResolver.AtVersionCorrection underlying = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
    final Set<UniqueId> expiredResolutions = new HashSet<UniqueId>();
    final ComputationTargetResolver.AtVersionCorrection resolver = TargetResolutionLogger.of(underlying, resolutions, expiredResolutions);
    final ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Foo", "Bar"));
    final ComputationTarget target = new ComputationTarget(spec.replaceIdentifier(UniqueId.of("Foo", "Bar", "Cow")), new Primitive(UniqueId.of("Foo", "Bar", "Cow")));
    Mockito.when(underlying.resolve(spec)).thenReturn(target);
    final ObjectResolver deepResolver = Mockito.mock(ObjectResolver.class);
    Mockito.when(deepResolver.deepResolver()).thenReturn(new DeepResolver() {
      @SuppressWarnings("serial")
      @Override
      public UniqueIdentifiable withLogger(final UniqueIdentifiable underlying, final ResolutionLogger logger) {
        assertSame(underlying, target.getValue());
        return new Primitive(UniqueId.of("Foo", "Bar", "Cow")) {
          @Override
          public int hashCode() {
            // Pretend that this is a deep-resolving operation
            logger.log(spec, underlying.getUniqueId());
            return super.hashCode();
          }
        };
      }
    });
    Mockito.when(underlying.getResolver(spec)).thenReturn(deepResolver);
    final ComputationTarget resolvedTarget = resolver.resolve(spec);
    assertNotSame(resolvedTarget, target);
    assertTrue(resolutions.isEmpty());
    resolvedTarget.getValue().hashCode();
    assertFalse(resolutions.isEmpty());
  }

  private Map<ComputationTargetReference, ComputationTargetSpecification> targets() {
    final Map<ComputationTargetReference, ComputationTargetSpecification> map = new HashMap<ComputationTargetReference, ComputationTargetSpecification>();
    map.put(new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Test", "1")), ComputationTargetSpecification.NULL);
    map.put(new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Test", "2")), null);
    map.put(new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "3")),
        new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "3", "X")));
    map.put(new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "4", "X")),
        new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "4", "X")));
    map.put(new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Foo", "Bar")),
        new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "Bar", "OLD")));
    return map;
  }

  public void testGetSpecificationResolver_single() {
    final ComputationTargetResolver.AtVersionCorrection underlying = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
    final Set<UniqueId> expiredResolutions = new HashSet<UniqueId>();
    final ComputationTargetSpecificationResolver.AtVersionCorrection underlyingResolver = Mockito.mock(ComputationTargetSpecificationResolver.AtVersionCorrection.class);
    Mockito.when(underlying.getSpecificationResolver()).thenReturn(underlyingResolver);
    final ComputationTargetResolver.AtVersionCorrection targetResolver = TargetResolutionLogger.of(underlying, resolutions, expiredResolutions);
    final ComputationTargetSpecificationResolver.AtVersionCorrection specificationResolver = targetResolver.getSpecificationResolver();
    assertNotNull(specificationResolver);
    final Map<ComputationTargetReference, ComputationTargetSpecification> targets = targets();
    for (Map.Entry<ComputationTargetReference, ComputationTargetSpecification> target : targets.entrySet()) {
      Mockito.when(underlyingResolver.getTargetSpecification(target.getKey())).thenReturn(target.getValue());
    }
    for (Map.Entry<ComputationTargetReference, ComputationTargetSpecification> target : targets.entrySet()) {
      assertSame(specificationResolver.getTargetSpecification(target.getKey()), target.getValue());
    }
    assertEquals(resolutions.size(), 2);
    assertTrue(resolutions.containsValue(UniqueId.of("Test", "3", "X")));
    assertTrue(resolutions.containsValue(UniqueId.of("Test", "Bar", "OLD")));
    assertEquals(expiredResolutions.size(), 0);
    final ComputationTargetReference ref = new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Foo", "Bar"));
    final ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Test", "Bar", "NEW"));
    Mockito.when(underlyingResolver.getTargetSpecification(ref)).thenReturn(spec);
    assertSame(specificationResolver.getTargetSpecification(ref), spec);
    assertEquals(resolutions.size(), 2);
    assertTrue(resolutions.containsValue(UniqueId.of("Test", "3", "X")));
    assertTrue(resolutions.containsValue(UniqueId.of("Test", "Bar", "NEW")));
    assertEquals(expiredResolutions, Collections.singleton(UniqueId.of("Test", "Bar", "OLD")));
  }

  public void testGetSpecificationResolver_multi() {
    final ComputationTargetResolver.AtVersionCorrection underlying = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
    final Set<UniqueId> expiredResolutions = new HashSet<UniqueId>();
    final ComputationTargetSpecificationResolver.AtVersionCorrection underlyingResolver = Mockito.mock(ComputationTargetSpecificationResolver.AtVersionCorrection.class);
    Mockito.when(underlying.getSpecificationResolver()).thenReturn(underlyingResolver);
    final ComputationTargetResolver.AtVersionCorrection targetResolver = TargetResolutionLogger.of(underlying, resolutions, expiredResolutions);
    final ComputationTargetSpecificationResolver.AtVersionCorrection specificationResolver = targetResolver.getSpecificationResolver();
    assertNotNull(specificationResolver);
    final Map<ComputationTargetReference, ComputationTargetSpecification> targets = targets();
    final Set<ComputationTargetReference> refs = new HashSet<ComputationTargetReference>(targets.keySet());
    targets.remove(new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Test", "2")));
    Mockito.when(underlyingResolver.getTargetSpecifications(refs)).thenReturn(targets);
    assertSame(specificationResolver.getTargetSpecifications(refs), targets);
    assertEquals(resolutions.size(), 2);
    assertTrue(resolutions.containsValue(UniqueId.of("Test", "3", "X")));
    assertTrue(resolutions.containsValue(UniqueId.of("Test", "Bar", "OLD")));
    assertEquals(expiredResolutions.size(), 0);
  }

  public void testGetSpecificationResolver_null() {
    final ComputationTargetResolver.AtVersionCorrection underlying = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    final ComputationTargetSpecificationResolver.AtVersionCorrection underlyingResolver = Mockito.mock(ComputationTargetSpecificationResolver.AtVersionCorrection.class);
    Mockito.when(underlying.getSpecificationResolver()).thenReturn(underlyingResolver);
    final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
    final Set<UniqueId> expiredResolutions = new HashSet<UniqueId>();
    final ComputationTargetResolver.AtVersionCorrection targetResolver = TargetResolutionLogger.of(underlying, resolutions, expiredResolutions);
    Mockito.when(underlyingResolver.getTargetSpecification(ComputationTargetSpecification.NULL)).thenReturn(ComputationTargetSpecification.NULL);
    assertSame(targetResolver.getSpecificationResolver().getTargetSpecification(ComputationTargetSpecification.NULL), ComputationTargetSpecification.NULL);
    assertTrue(resolutions.isEmpty());
    assertTrue(expiredResolutions.isEmpty());
  }

  public void testGetSpecificationResolver_complex() {
    final ComputationTargetResolver.AtVersionCorrection underlying = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    final ComputationTargetSpecificationResolver.AtVersionCorrection underlyingResolver = Mockito.mock(ComputationTargetSpecificationResolver.AtVersionCorrection.class);
    Mockito.when(underlying.getSpecificationResolver()).thenReturn(underlyingResolver);
    final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
    final Set<UniqueId> expiredResolutions = new HashSet<UniqueId>();
    final ComputationTargetResolver.AtVersionCorrection targetResolver = TargetResolutionLogger.of(underlying, resolutions, expiredResolutions);
    final ComputationTargetSpecification spec1 = ComputationTargetSpecification.of(UniqueId.of("Test", "1")).containing(
        ComputationTargetType.multiple(ComputationTargetType.POSITION, ComputationTargetType.SECURITY), UniqueId.of("Test", "2"));
    final ComputationTargetSpecification spec1V = ComputationTargetSpecification.of(UniqueId.of("Test", "1", "V")).containing(ComputationTargetType.POSITION, UniqueId.of("Test", "2", "V"));
    Mockito.when(underlyingResolver.getTargetSpecification(spec1)).thenReturn(spec1V);
    final ComputationTargetSpecification spec2 = new ComputationTargetSpecification(new ComputationTargetSpecification(ComputationTargetType.POSITION_OR_TRADE, UniqueId.of("Test", "4", "V")),
        ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE).or(ComputationTargetType.TRADE.containing(ComputationTargetType.SECURITY)), UniqueId.of("Test", "3"));
    final ComputationTargetSpecification spec2V = new ComputationTargetSpecification(new ComputationTargetSpecification(ComputationTargetType.POSITION_OR_TRADE, UniqueId.of("Test", "4", "V")),
        ComputationTargetType.POSITION.containing(ComputationTargetType.TRADE).or(ComputationTargetType.TRADE.containing(ComputationTargetType.SECURITY)), UniqueId.of("Test", "3", "V"));
    Mockito.when(underlyingResolver.getTargetSpecification(spec2)).thenReturn(spec2V);
    assertSame(targetResolver.getSpecificationResolver().getTargetSpecification(spec1), spec1V);
    assertSame(targetResolver.getSpecificationResolver().getTargetSpecification(spec2), spec2V);
  }

  private interface TestSecurity extends Security {
  }

  public void testSimplifyType() {
    final ComputationTargetResolver.AtVersionCorrection underlying = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
    final Set<UniqueId> expiredResolutions = new HashSet<UniqueId>();
    final ComputationTargetResolver.AtVersionCorrection resolver = TargetResolutionLogger.of(underlying, resolutions, expiredResolutions);
    final ComputationTargetType type = ComputationTargetType.of(TestSecurity.class);
    Mockito.when(underlying.simplifyType(type)).thenReturn(ComputationTargetType.SECURITY);
    assertSame(resolver.simplifyType(type), ComputationTargetType.SECURITY);
  }

  public void testGetVersionCorrection() {
    final ComputationTargetResolver.AtVersionCorrection underlying = Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class);
    final ConcurrentMap<ComputationTargetReference, UniqueId> resolutions = new ConcurrentHashMap<ComputationTargetReference, UniqueId>();
    final Set<UniqueId> expiredResolutions = new HashSet<UniqueId>();
    final ComputationTargetResolver.AtVersionCorrection resolver = TargetResolutionLogger.of(underlying, resolutions, expiredResolutions);
    final VersionCorrection vc = VersionCorrection.of(Instant.now(), Instant.now());
    Mockito.when(underlying.getVersionCorrection()).thenReturn(vc);
    assertSame(resolver.getVersionCorrection(), vc);
  }

}
