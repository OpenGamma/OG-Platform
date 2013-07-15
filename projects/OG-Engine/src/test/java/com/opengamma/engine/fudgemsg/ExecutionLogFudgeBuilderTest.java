/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.calcnode.MutableExecutionLog;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExecutionLogFudgeBuilderTest {

  @Test
  public void roundTrip() {
    MutableExecutionLog log = new MutableExecutionLog(ExecutionLogMode.FULL);
    LogEvent errorEvent = new SimpleLogEvent(LogLevel.ERROR, "error msg");
    LogEvent infoEvent1 = new SimpleLogEvent(LogLevel.INFO, "info msg");
    LogEvent infoEvent2 = new SimpleLogEvent(LogLevel.INFO, "info msg2");
    log.add(errorEvent);
    log.add(infoEvent1);
    log.add(infoEvent2);
    String execptionMsg = "exception msg";
    assertEquals(log.getLogLevels(), EnumSet.of(LogLevel.ERROR, LogLevel.INFO));
    log.setException(new OpenGammaRuntimeException(execptionMsg));
    assertEquals(log.getLogLevels(), EnumSet.of(LogLevel.ERROR, LogLevel.INFO, LogLevel.WARN));
    FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    ExecutionLogFudgeBuilder builder = new ExecutionLogFudgeBuilder();
    FudgeMsg msg = builder.buildMessage(serializer, log);

    FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    ExecutionLog executionLog = builder.buildObject(deserializer, msg);
    assertEquals(EnumSet.<LogLevel>of(LogLevel.ERROR, LogLevel.INFO, LogLevel.WARN), executionLog.getLogLevels());
    List<LogEvent> events = executionLog.getEvents();
    assertTrue(events.contains(errorEvent));
    assertTrue(events.contains(infoEvent1));
    assertTrue(events.contains(infoEvent2));
    assertEquals("com.opengamma.OpenGammaRuntimeException", executionLog.getExceptionClass());
    assertEquals(execptionMsg, executionLog.getExceptionMessage());
    assertFalse(StringUtils.isEmpty(executionLog.getExceptionStackTrace()));
  }
}
