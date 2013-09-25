/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.resolver.AbstractIdentifierResolver;
import com.opengamma.engine.target.resolver.PrimitiveResolver;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DefaultComputationTargetSpecificationResolver} class.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultComputationTargetSpecificationResolverTest {

  private static class Foo implements UniqueIdentifiable {

    @Override
    public UniqueId getUniqueId() {
      throw new IllegalStateException();
    }

  }

  private static class Bar implements UniqueIdentifiable {

    @Override
    public UniqueId getUniqueId() {
      throw new IllegalStateException();
    }

  }

  private final VersionCorrection VC = VersionCorrection.of(Instant.now(), Instant.now());
  private final DefaultComputationTargetSpecificationResolver RESOLVER = new DefaultComputationTargetSpecificationResolver();
  private final ComputationTargetSpecification SPECIFICATION_NULL = ComputationTargetSpecification.NULL;
  private final ComputationTargetSpecification SPECIFICATION_PRIMITIVE_VERSIONED = ComputationTargetSpecification.of(UniqueId.of("ExternalId-Test", "X", "V"));
  private final ComputationTargetSpecification SPECIFICATION_PRIMITIVE_LATEST = ComputationTargetSpecification.of(UniqueId.of("ExternalId-Test", "X"));
  private final ComputationTargetSpecification SPECIFICATION_FOO_VERSIONED = new ComputationTargetSpecification(ComputationTargetType.of(Foo.class), UniqueId.of("Foo", "Bar", "V"));
  private final ComputationTargetSpecification SPECIFICATION_FOO_LATEST = new ComputationTargetSpecification(ComputationTargetType.of(Foo.class), UniqueId.of("Foo", "Bar"));
  private final ComputationTargetSpecification SPECIFICATION_BAR_LATEST = new ComputationTargetSpecification(ComputationTargetType.of(Bar.class), UniqueId.of("Bar", "Foo"));
  private final ComputationTargetSpecification SPECIFICATION_FOO_BAD = new ComputationTargetSpecification(ComputationTargetType.of(Foo.class), UniqueId.of("Foo", "Cow"));
  private final ComputationTargetSpecification SPECIFICATION_FOO_OR_BAR = new ComputationTargetSpecification(ComputationTargetType.of(Foo.class).or(ComputationTargetType.of(Bar.class)), UniqueId.of(
      "Foo", "Bar"));
  private final ComputationTargetSpecification SPECIFICATION_FOO_OR_BAR_BAD = new ComputationTargetSpecification(ComputationTargetType.of(Foo.class).or(ComputationTargetType.of(Bar.class)),
      UniqueId.of("Foo", "Cow"));
  private final ComputationTargetSpecification SPECIFICATION_FOO_OR_BAR_VERSIONED = new ComputationTargetSpecification(ComputationTargetType.of(Foo.class).or(ComputationTargetType.of(Bar.class)),
      UniqueId.of("Foo", "Bar", "V"));
  private final ComputationTargetRequirement REQUIREMENT_PRIMITIVE = new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalIdBundle.of(ExternalId.of("Test", "X")));
  private final ComputationTargetRequirement REQUIREMENT_FOO_VALID = new ComputationTargetRequirement(ComputationTargetType.of(Foo.class), ExternalIdBundle.of(ExternalId.of("Test", "B")));
  private final ComputationTargetRequirement REQUIREMENT_FOO_INVALID = new ComputationTargetRequirement(ComputationTargetType.of(Foo.class), ExternalIdBundle.of(ExternalId.of("Test", "C")));
  private final ComputationTargetRequirement REQUIREMENT_FOO_OR_BAR = new ComputationTargetRequirement(ComputationTargetType.of(Foo.class).or(ComputationTargetType.of(Bar.class)), ExternalId.of(
      "Test", "B"));
  private final ComputationTargetRequirement REQUIREMENT_FOO_OR_BAR_BAD = new ComputationTargetRequirement(ComputationTargetType.of(Foo.class).or(ComputationTargetType.of(Bar.class)), ExternalId.of(
      "Test", "C"));

  public DefaultComputationTargetSpecificationResolverTest() {
    RESOLVER.addResolver(ComputationTargetType.of(Foo.class), new AbstractIdentifierResolver() {

      @Override
      public UniqueId resolveExternalId(final ExternalIdBundle identifiers, final VersionCorrection versionCorrection) {
        assertEquals(versionCorrection, VC);
        if (identifiers.contains(ExternalId.of("Test", "B"))) {
          return SPECIFICATION_FOO_VERSIONED.getUniqueId();
        } else {
          return null;
        }
      }

      @Override
      public UniqueId resolveObjectId(final ObjectId identifier, final VersionCorrection versionCorrection) {
        assertEquals(versionCorrection, VC);
        if (identifier.getValue().equals("Bar")) {
          return SPECIFICATION_FOO_VERSIONED.getUniqueId();
        } else {
          return null;
        }
      }

    });
    RESOLVER.addResolver(ComputationTargetType.PRIMITIVE, new PrimitiveResolver());
  }

  public void testSpecification_null() {
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_NULL, VC), SPECIFICATION_NULL);
  }

  public void testSpecification_versioned() {
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_PRIMITIVE_VERSIONED, VC), SPECIFICATION_PRIMITIVE_VERSIONED);
  }

  public void testSpecification_latestNoResolver() {
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_BAR_LATEST, VC), SPECIFICATION_BAR_LATEST);
  }

  public void testSpecification_latestResolved() {
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_FOO_LATEST, VC), SPECIFICATION_FOO_VERSIONED);
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_PRIMITIVE_LATEST, VC), SPECIFICATION_PRIMITIVE_LATEST);
  }

  public void testSpecification_latestUnresolved() {
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_FOO_BAD, VC), null);
  }

  public void testSpecification_unionType() {
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_FOO_OR_BAR, VC), SPECIFICATION_FOO_VERSIONED);
  }

  public void testSpecification_unionTypeUnresolved() {
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_FOO_OR_BAR_BAD, VC), null);
  }

  public void testSpecification_unionTypeVersioned() {
    //assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_FOO_OR_BAR_VERSIONED, VC), SPECIFICATION_FOO_VERSIONED);
    assertEquals(RESOLVER.getTargetSpecification(SPECIFICATION_FOO_OR_BAR_VERSIONED, VC), SPECIFICATION_FOO_OR_BAR_VERSIONED);
  }

  public void testRequirement_noResolver() {
    assertEquals(RESOLVER.getTargetSpecification(REQUIREMENT_PRIMITIVE, VC), SPECIFICATION_PRIMITIVE_LATEST);
  }

  public void testRequirement_resolved() {
    assertEquals(RESOLVER.getTargetSpecification(REQUIREMENT_FOO_VALID, VC), SPECIFICATION_FOO_VERSIONED);
  }

  public void testRequirement_unresolved() {
    assertEquals(RESOLVER.getTargetSpecification(REQUIREMENT_FOO_INVALID, VC), null);
  }

  public void testRequirement_unionType() {
    assertEquals(RESOLVER.getTargetSpecification(REQUIREMENT_FOO_OR_BAR, VC), SPECIFICATION_FOO_VERSIONED);
  }

  public void testRequirement_unionTypeUnresolved() {
    assertEquals(RESOLVER.getTargetSpecification(REQUIREMENT_FOO_OR_BAR_BAD, VC), null);
  }

  public void testAll() {
    final Set<ComputationTargetReference> request = new HashSet<ComputationTargetReference>();
    request.add(SPECIFICATION_NULL);
    request.add(SPECIFICATION_PRIMITIVE_VERSIONED);
    request.add(SPECIFICATION_PRIMITIVE_LATEST);
    request.add(SPECIFICATION_FOO_LATEST);
    request.add(SPECIFICATION_BAR_LATEST);
    request.add(SPECIFICATION_FOO_BAD);
    request.add(SPECIFICATION_FOO_OR_BAR);
    request.add(SPECIFICATION_FOO_OR_BAR_BAD);
    request.add(SPECIFICATION_FOO_OR_BAR_VERSIONED);
    request.add(REQUIREMENT_PRIMITIVE);
    request.add(REQUIREMENT_FOO_VALID);
    request.add(REQUIREMENT_FOO_INVALID);
    request.add(REQUIREMENT_FOO_OR_BAR);
    request.add(REQUIREMENT_FOO_OR_BAR_BAD);
    final Map<ComputationTargetReference, ComputationTargetSpecification> result = RESOLVER.getTargetSpecifications(request, VC);
    assertEquals(result.get(SPECIFICATION_NULL), SPECIFICATION_NULL);
    assertEquals(result.get(SPECIFICATION_PRIMITIVE_VERSIONED), SPECIFICATION_PRIMITIVE_VERSIONED);
    assertEquals(result.get(SPECIFICATION_PRIMITIVE_LATEST), SPECIFICATION_PRIMITIVE_LATEST);
    assertEquals(result.get(SPECIFICATION_FOO_LATEST), SPECIFICATION_FOO_VERSIONED);
    assertEquals(result.get(SPECIFICATION_BAR_LATEST), SPECIFICATION_BAR_LATEST);
    assertEquals(result.get(SPECIFICATION_FOO_BAD), null);
    assertEquals(result.get(SPECIFICATION_FOO_OR_BAR), SPECIFICATION_FOO_VERSIONED);
    assertEquals(result.get(SPECIFICATION_FOO_OR_BAR_BAD), null);
    //assertEquals(result.get(SPECIFICATION_FOO_OR_BAR_VERSIONED), SPECIFICATION_FOO_VERSIONED);
    assertEquals(result.get(SPECIFICATION_FOO_OR_BAR_VERSIONED), SPECIFICATION_FOO_OR_BAR_VERSIONED);
    assertEquals(result.get(REQUIREMENT_PRIMITIVE), SPECIFICATION_PRIMITIVE_LATEST);
    assertEquals(result.get(REQUIREMENT_FOO_VALID), SPECIFICATION_FOO_VERSIONED);
    assertEquals(result.get(REQUIREMENT_FOO_INVALID), null);
    assertEquals(result.get(REQUIREMENT_FOO_OR_BAR), SPECIFICATION_FOO_VERSIONED);
    assertEquals(result.get(REQUIREMENT_FOO_OR_BAR_BAD), null);
  }

}
