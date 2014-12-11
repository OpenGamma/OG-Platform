/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;
import org.joda.beans.Bean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FlexiBeanFudgeBuilder} class.
 */
@Test(groups = TestGroup.UNIT)
public class JodaBeanFudgeBuilderTest extends AbstractFudgeBuilderTestCase {

  @Test
  public void test_Joda_MapWithTypeObject() throws IOException {
    // bean
    Set<String> strings = ImmutableSet.of("a", "b", "c");
    Bean bean = JodaTestBean.builder().map(ImmutableMap.<String, Object>of("some", strings)).build();
    // write
    FudgeContext ctx = OpenGammaFudgeContext.getInstance();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Writer writer = new OutputStreamWriter(baos)) {
      FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(ctx, writer);
      try (FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(streamWriter)) {
        MutableFudgeMsg msg = (new FudgeSerializer(ctx)).objectToFudgeMsg(bean);
        FudgeSerializer.addClassHeader(msg, bean.getClass());
        fudgeMsgWriter.writeMessage(msg);
        fudgeMsgWriter.flush();
      }
    }
    // read
    try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()))) {
      try (FudgeXMLStreamReader xmlReader = new FudgeXMLStreamReader(ctx, reader)) {
        try (FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(xmlReader)) {
          FudgeDeserializer deserializer = new FudgeDeserializer(ctx);
          Bean cycled = deserializer.fudgeMsgToObject(bean.getClass(), fudgeMsgReader.nextMessage());
          assertEquals(cycled, bean);
        }
      }
    }
  }

  @Test
  public void test_Joda_EmptyMapWithTypeObject() throws IOException {
    // bean
    Bean bean = JodaTestBean.builder().map(ImmutableMap.<String, Object>of()).build();
    // write
    FudgeContext ctx = OpenGammaFudgeContext.getInstance();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Writer writer = new OutputStreamWriter(baos)) {
      FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(ctx, writer);
      try (FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(streamWriter)) {
        MutableFudgeMsg msg = (new FudgeSerializer(ctx)).objectToFudgeMsg(bean);
        FudgeSerializer.addClassHeader(msg, bean.getClass());
        fudgeMsgWriter.writeMessage(msg);
        fudgeMsgWriter.flush();
      }
    }
    // read
    try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()))) {
      try (FudgeXMLStreamReader xmlReader = new FudgeXMLStreamReader(ctx, reader)) {
        try (FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(xmlReader)) {
          FudgeDeserializer deserializer = new FudgeDeserializer(ctx);
          Bean cycled = deserializer.fudgeMsgToObject(bean.getClass(), fudgeMsgReader.nextMessage());
          assertEquals(cycled, bean);
        }
      }
    }
  }

  @Test
  public void test_Joda_ObjectField() throws IOException {
    // bean
    Set<String> strings = ImmutableSet.of("a", "b", "c");
    Bean bean = JodaTestBean.builder().object(ImmutableMap.<String, Object>of("some", strings)).build();
    // write
    FudgeContext ctx = OpenGammaFudgeContext.getInstance();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (Writer writer = new OutputStreamWriter(baos)) {
      FudgeXMLStreamWriter streamWriter = new FudgeXMLStreamWriter(ctx, writer);
      try (FudgeMsgWriter fudgeMsgWriter = new FudgeMsgWriter(streamWriter)) {
        MutableFudgeMsg msg = (new FudgeSerializer(ctx)).objectToFudgeMsg(bean);
        FudgeSerializer.addClassHeader(msg, bean.getClass());
        fudgeMsgWriter.writeMessage(msg);
        fudgeMsgWriter.flush();
      }
    }
    // read
    try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()))) {
      try (FudgeXMLStreamReader xmlReader = new FudgeXMLStreamReader(ctx, reader)) {
        try (FudgeMsgReader fudgeMsgReader = new FudgeMsgReader(xmlReader)) {
          FudgeDeserializer deserializer = new FudgeDeserializer(ctx);
          Bean cycled = deserializer.fudgeMsgToObject(bean.getClass(), fudgeMsgReader.nextMessage());
          assertEquals(cycled, bean);
        }
      }
    }
  }

}
