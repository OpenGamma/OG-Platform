/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.custom;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the mapping of message classes to visitor implementations.
 */
@Test(groups = TestGroup.UNIT)
public class CustomVisitorsTest {

  private class CustomMessage extends com.opengamma.language.connector.Custom {

    private static final long serialVersionUID = 1L;

  }

  private class CustomSubMessage extends CustomMessage {

    private static final long serialVersionUID = 1L;

  }

  private class CustomUnregisteredMessage extends com.opengamma.language.connector.Custom {

    private static final long serialVersionUID = 1L;

  }

  private class CustomFunction extends com.opengamma.language.function.Custom {

    private static final long serialVersionUID = 1L;

  }

  private class CustomLiveData extends com.opengamma.language.livedata.Custom {

    private static final long serialVersionUID = 1L;

  }

  private class CustomProcedure extends com.opengamma.language.procedure.Custom {

    private static final long serialVersionUID = 1L;

  }

  private void registerVisitors(final CustomVisitors<String, Integer> visitors) {
    visitors.register(CustomMessage.class, new CustomMessageVisitor<CustomMessage, String, Integer>() {
      @Override
      public String visit(CustomMessage message, Integer data) {
        assertNotNull(message);
        assertNotNull(data);
        return "M" + message.getClass().getSimpleName() + data;
      }
    });
    visitors.register(CustomFunction.class, new CustomFunctionVisitor<CustomFunction, String, Integer>() {
      @Override
      public String visit(CustomFunction message, Integer data) {
        assertNotNull(message);
        assertNotNull(data);
        return "F" + message.getClass().getSimpleName() + data;
      }
    });
    visitors.register(CustomLiveData.class, new CustomLiveDataVisitor<CustomLiveData, String, Integer>() {
      @Override
      public String visit(CustomLiveData message, Integer data) {
        assertNotNull(message);
        assertNotNull(data);
        return "L" + message.getClass().getSimpleName() + data;
      }
    });
    visitors.register(CustomProcedure.class, new CustomProcedureVisitor<CustomProcedure, String, Integer>() {
      @Override
      public String visit(CustomProcedure message, Integer data) {
        assertNotNull(message);
        assertNotNull(data);
        return "P" + message.getClass().getSimpleName() + data;
      }
    });
  }

  @Test
  public void testBasicBehaviour() {
    final CustomVisitors<String, Integer> visitors = new CustomVisitors<String, Integer>();
    registerVisitors(visitors);
    assertEquals("MCustomMessage1", visitors.visit(new CustomMessage(), 1));
    assertEquals("FCustomFunction2", visitors.visit(new CustomFunction(), 2));
    assertEquals("LCustomLiveData3", visitors.visit(new CustomLiveData(), 3));
    assertEquals("PCustomProcedure4", visitors.visit(new CustomProcedure(), 4));
  }

  @Test
  public void testInheritanceBehaviour() {
    final CustomVisitors<String, Integer> visitors = new CustomVisitors<String, Integer>();
    registerVisitors(visitors);
    assertEquals("MCustomSubMessage42", visitors.visit(new CustomSubMessage(), 42));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoVisitor1() {
    final CustomVisitors<String, Integer> visitors = new CustomVisitors<String, Integer>();
    assertEquals("MCustomMessage42", visitors.visit(new CustomMessage(), 42));
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNoVisitor2() {
    final CustomVisitors<String, Integer> visitors = new CustomVisitors<String, Integer>();
    registerVisitors(visitors);
    assertEquals("MCustomUnregisteredMessage42", visitors.visit(new CustomUnregisteredMessage(), 42));
  }

}
