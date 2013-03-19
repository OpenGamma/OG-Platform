/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.external;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.lang.annotation.ExternalFunction;
import com.opengamma.lang.annotation.ExternalFunctionParam;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.Parameter;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.Result;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ExternalFunctionHandler} class
 */
@Test(groups = TestGroup.UNIT)
public class ExternalFunctionHandlerTest {
  
  private static final Logger s_logger=LoggerFactory.getLogger(ExternalFunctionHandlerTest.class);

  private Set<String> getExportedFunctions(final Class<?> clazz) {
    final ExternalFunctionHandler handler = new ExternalFunctionHandler(clazz);
    final Set<String> functions = new HashSet<String>();
    for (MetaFunction function : handler.getFunctions()) {
      functions.add(function.getName());
    }
    s_logger.debug("{} functions = {}", clazz.getSimpleName (), functions);
    return functions;
  }

  public static class SingleStatic {

    @ExternalFunction
    public static int func(int a) {
      return 42;
    }

  }

  public void testSingleStatic() {
    final Set<String> functions = getExportedFunctions(SingleStatic.class);
    assertEquals(functions.size(), 1);
    assertTrue(functions.contains("SingleStatic.func"));
  }

  public static class SingleInstance {

    @ExternalFunction
    public int func(int a) {
      return 42;
    }

  }

  public void testSingleInstance() {
    final Set<String> functions = getExportedFunctions(SingleInstance.class);
    assertEquals(functions.size(), 1);
    assertTrue(functions.contains("SingleInstance.func"));
  }

  public static class SingleInstanceNoDefault {

    public SingleInstanceNoDefault(int ignored) {
    }

    @ExternalFunction
    public int func(int a) {
      return 42;
    }

  }

  public void testSingleInstanceNoDefault() {
    final Set<String> functions = getExportedFunctions(SingleInstanceNoDefault.class);
    assertEquals(functions.size(), 0);
  }

  public static class Mixed {

    @ExternalFunction
    public static int funcA(int a) {
      return 42;
    }

    @ExternalFunction
    public int funcB(int a) {
      return 42;
    }

  }

  public void testMixed() {
    final Set<String> functions = getExportedFunctions(Mixed.class);
    assertEquals(functions.size(), 2);
    assertTrue(functions.contains("Mixed.funcA"));
    assertTrue(functions.contains("Mixed.funcB"));
  }

  public static class MixedNoDefault {

    public MixedNoDefault(int ignored) {
    }

    @ExternalFunction
    public static int funcA(int a) {
      return 42;
    }

    @ExternalFunction
    public int funcB(int a) {
      return 42;
    }

  }

  public void testMixedNoDefault() {
    final Set<String> functions = getExportedFunctions(MixedNoDefault.class);
    assertEquals(functions.size(), 1);
    assertTrue(functions.contains("MixedNoDefault.funcA"));
  }

  public static class Constructed {

    private final int _a;

    @ExternalFunction
    public Constructed(final int a) {
      _a = a;
    }

    @ExternalFunction
    public static int getA(final Constructed object) {
      return object._a;
    }

    public FudgeMsg toFudgeMsg(final FudgeMsgFactory factory) {
      final MutableFudgeMsg msg = factory.newMessage();
      msg.add("a", _a);
      return msg;
    }

    public static Constructed fromFudgeMsg(final FudgeMsg msg) {
      return new Constructed(msg.getInt("a"));
    }

  }

  public void testConstructed() {
    final Set<String> functions = getExportedFunctions(Constructed.class);
    assertEquals(functions.size(), 2);
    assertTrue(functions.contains("Constructed"));
    assertTrue(functions.contains("Constructed.getA"));
  }

  public static class InvalidConstructed {

    private final int _a;

    @ExternalFunction
    public InvalidConstructed(final int a) {
      _a = a;
    }

    @ExternalFunction
    public int getA() {
      return _a;
    }

  }

  public void testInvalidConstructed() {
    final Set<String> functions = getExportedFunctions(InvalidConstructed.class);
    assertEquals(functions.size(), 1);
    assertTrue(functions.contains("InvalidConstructed"));
  }
  
  public static class WithoutAttributes {

    @ExternalFunction
    public int func(Map<String, String> param1) {
      return 42;
    }

  }

  public void testWithoutAttributes() {
    final ExternalFunctionHandler handler = new ExternalFunctionHandler(WithoutAttributes.class);
    final MetaFunction function = handler.getFunctions().iterator().next();
    assertEquals(function.getName(), "WithoutAttributes.func");
    assertEquals(function.getDescription(), null);
    assertEquals(function.getCategory(), null);
    assertEquals(function.getAlias(), Arrays.asList("com.opengamma.language.external.ExternalFunctionHandlerTest$WithoutAttributes.func"));
    final List<Parameter> parameters = function.getParameter();
    assertEquals(parameters.size(), 1);
    final MetaParameter param1 = (MetaParameter) parameters.get(0);
    assertEquals(param1.getName(), "param1");
    assertEquals(param1.getDescription(), "The first parameter");
    assertEquals(param1.getRequired(), false);
    assertEquals(param1.getJavaTypeInfo(), JavaTypeInfo.builder(Map.class).allowNull().get());
  }

  public static class WithAttributes {
    
    @ExternalFunction(name = "Name", description = "The description", category = Categories.MISC, alias = { "Foo", "Bar" })
    public int func(@ExternalFunctionParam(allowNull = false, name = "param1", description = "Description 1", type = "java.util.Map<java.lang.String,java.util.Currency>") Map<String, String> param1) {
      return 42;
    }
    
  }
  
  public void testWithAttributes() {
    final ExternalFunctionHandler handler = new ExternalFunctionHandler(WithAttributes.class);
    final MetaFunction function = handler.getFunctions().iterator().next();
    assertEquals(function.getName(), "Name");
    assertEquals(function.getDescription(), "The description");
    assertEquals(function.getCategory(), Categories.MISC);
    assertEquals(function.getAlias(), Arrays.asList("Foo", "Bar"));
    final List<Parameter> parameters = function.getParameter();
    assertEquals(parameters.size(), 1);
    final MetaParameter param1 = (MetaParameter) parameters.get(0);
    assertEquals(param1.getName(), "param1");
    assertEquals(param1.getDescription(), "Description 1");
    assertEquals(param1.getRequired(), true);
    assertEquals(param1.getJavaTypeInfo(), JavaTypeInfo.builder(Map.class).parameter(String.class).parameter(Currency.class).get());
  }
  
  public void testInvoker() throws Exception {
    final TestUtils testUtil = new TestUtils();
    final ExternalFunctionHandler handler = new ExternalFunctionHandler(Constructed.class);
    MetaFunction cons = null;
    MetaFunction mtd = null;
    for (MetaFunction fn : handler.getFunctions()) {
      if ("Constructed".equals(fn.getName())) {
        cons = fn;
      } else if ("Constructed.getA".equals(fn.getName())) {
        mtd = fn;
      }
    }
    assertNotNull(cons);
    assertNotNull(mtd);
    testUtil.setTypeConverters(new Converters());
    final SessionContext sessionContext = testUtil.createSessionContext();
    Result result = cons.getInvoker().invoke(sessionContext, Arrays.asList(DataUtils.of(69)));
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertEquals(result.getResult().size(), 1);
    Data obj = result.getResult().get(0);
    assertNotNull(obj.getSingle());
    assertEquals(obj.getSingle().getStringValue(), null);
    assertEquals(obj.getSingle().getErrorValue(), null);
    assertNotNull(obj.getSingle().getMessageValue());
    result = mtd.getInvoker().invoke(sessionContext, Arrays.asList(obj));
    assertNotNull(result);
    assertNotNull(result.getResult());
    assertEquals(result.getResult().size(), 1);
    obj = result.getResult().get(0);
    assertEquals(obj, DataUtils.of(69));
  }

}
