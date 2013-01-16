/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Tests the {@link ComputationTargetReferenceFudgeBuilder} class.
 */
@Test
public class ComputationTargetReferenceFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  public void testRequirement() {
    assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, ExternalId.of("Foo", "Bar")));
  }

  public void testSpecification() {
    assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar")));
  }

  public void testRequirement_null() {
    assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetRequirement(ComputationTargetType.NULL, (ExternalId) null));
  }

  public void testSpecification_null() {
    assertEncodeDecodeCycle(ComputationTargetReference.class, new ComputationTargetSpecification(ComputationTargetType.NULL, null));
  }

  public void testRequirement_nested() {
    ComputationTargetRequirement req = new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Bar"));
    req = req.containing(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Underlying"));
    assertEncodeDecodeCycle(ComputationTargetReference.class, req);
  }

  public void testSpecification_nested() {
    ComputationTargetSpecification req = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar"));
    req = req.containing(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Underlying"));
    assertEncodeDecodeCycle(ComputationTargetReference.class, req);
  }

}
