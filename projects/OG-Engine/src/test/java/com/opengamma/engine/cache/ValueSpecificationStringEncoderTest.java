/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ValueSpecificationStringEncoder} class
 */
@Test(groups = TestGroup.UNIT)
public class ValueSpecificationStringEncoderTest {

  public void testBasic() {
    final ValueSpecification spec = new ValueSpecification("Value1", new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "A", "V1")), ValueProperties.with(
        ValuePropertyNames.FUNCTION, "Test").get());
    final String str = ValueSpecificationStringEncoder.encodeAsString(spec);
    assertEquals(str, "Value1,{Function=[Test]},(Sec~A~V1,com.opengamma.core.security.Security)");
  }

  public void testNullType() {
    final ValueSpecification spec = new ValueSpecification("Value2", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get());
    final String str = ValueSpecificationStringEncoder.encodeAsString(spec);
    assertEquals(str, "Value2,{Function=[Test]},(NULL,NULL)");
  }

  public void testMultipleTypes() {
    final ValueSpecification spec = new ValueSpecification("Value3",
        new ComputationTargetSpecification(ComputationTargetType.SECURITY.or(ComputationTargetType.POSITION), UniqueId.of("Obj", "A", "V1")), ValueProperties.with(ValuePropertyNames.FUNCTION, "Test")
            .get());
    final String str = ValueSpecificationStringEncoder.encodeAsString(spec);
    assertEquals(str, "Value3,{Function=[Test]},(Obj~A~V1,{com.opengamma.core.position.Position,com.opengamma.core.security.Security})");
  }

  public void testNestedTypes() {
    final ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Pos", "12"));
    final ComputationTargetRequirement b = a.containing(ComputationTargetType.SECURITY, ExternalIdBundle.of(ExternalId.of("B", "2"), ExternalId.of("A", "1")));
    final ComputationTargetSpecification c = b.containing(ComputationTargetType.PRIMITIVE, UniqueId.of("Foo", "Bar"));
    final ValueSpecification spec = new ValueSpecification("Value4", c, ValueProperties.with(ValuePropertyNames.FUNCTION, "Test").get());
    final String str = ValueSpecificationStringEncoder.encodeAsString(spec);
    assertEquals(str, "Value4,{Function=[Test]},(Pos~12,Bundle[A~1, B~2],Foo~Bar,[com.opengamma.core.position.Position,com.opengamma.core.security.Security,com.opengamma.engine.target.Primitive])");
  }

}
