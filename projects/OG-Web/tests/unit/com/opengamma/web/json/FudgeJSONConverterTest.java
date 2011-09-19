/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import static org.testng.AssertJUnit.assertNotNull;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.taxonomy.FudgeTaxonomy;
import org.fudgemsg.taxonomy.ImmutableMapTaxonomyResolver;
import org.fudgemsg.taxonomy.MapFudgeTaxonomy;
import org.fudgemsg.test.FudgeUtils;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Tests FudgeMsgJSONReader and FudgeMsgJSONWriter
 */
public class FudgeJSONConverterTest {

  private final FudgeContext _fudgeContext;

  private static FudgeTaxonomy getTaxonomy() {
    return new MapFudgeTaxonomy(
        new int[] {1, 2, 3, 4, 5, 6 },
        new String[] {"boolean", "byte", "int", "string", "float", "double" });
  }

  /**
   * 
   */
  public FudgeJSONConverterTest() {
    _fudgeContext = new FudgeContext();
    final Map<Short, FudgeTaxonomy> tr = new HashMap<Short, FudgeTaxonomy>();
    tr.put((short) 1, getTaxonomy());
    _fudgeContext.setTaxonomyResolver(new ImmutableMapTaxonomyResolver(tr));
  }

  private FudgeMsg[] createMessages() {
    return new FudgeMsg[] {
        createMessageAllNames(_fudgeContext), 
        createMessageAllOrdinals(_fudgeContext),
//        createMessageWithSubMsgs(_fudgeContext), 
//        createMessageAllByteArrayLengths(_fudgeContext)
    };
  }



  /**
   * 
   */
  @Test
  public void cycleJSONMessages_noTaxonomy() {
    System.out.println("cycleJSONMessages_noTaxonomy:");

    final FudgeMsg[] messages = createMessages();
    for (int i = 0; i < messages.length; i++) {
      System.out.println("orignal:" + messages[i]);
      
      final CharArrayWriter caw = new CharArrayWriter();
      final FudgeMsgJSONWriter fmw = new FudgeMsgJSONWriter(_fudgeContext, caw);
      fmw.writeMessage(messages[i], 0);
      fmw.flush();
      System.out.println("JSON:" + String.valueOf(caw.toCharArray()));
      
      final CharArrayReader car = new CharArrayReader(caw.toCharArray());
      final FudgeMsgJSONReader fmr = new FudgeMsgJSONReader(_fudgeContext, car);
      FudgeMsg message = fmr.readMessage();
      assertNotNull(message);
      System.out.println("from JSON:" + message);
      
      FudgeUtils.assertAllFieldsMatch(messages[i], message, false);
    }
  }

  /**
   * 
   */
  @Test
  public void cycleJSONMessages_withTaxonomy() {
    System.out.println("cycleJSONMessages_withTaxonomy:");

    final FudgeMsg[] messages = createMessages();
    for (int i = 0; i < messages.length; i++) {
      final CharArrayWriter caw = new CharArrayWriter();
      final FudgeMsgJSONWriter fmw = new FudgeMsgJSONWriter(_fudgeContext, caw);
      fmw.writeMessage(messages[i], 1);

      final CharArrayReader car = new CharArrayReader(caw.toCharArray());
      final FudgeMsgJSONReader fmr = new FudgeMsgJSONReader(_fudgeContext, car);
      fmr.getSettings().setPreserveFieldNames(false);
      
      FudgeMsg message = fmr.readMessage();
      assertNotNull(message);
      System.out.println("orignal:" + messages[i]);
      System.out.println("JSON:" + String.valueOf(caw.toCharArray()));
      System.out.println("from JSON:" + message);
      FudgeUtils.assertAllFieldsMatch(messages[i], message, false);
    }
  }
  
  /**
   * @param context [documentation not available]
   * @return [documentation not available]
   */
  public static MutableFudgeMsg createMessageAllNames(FudgeContext context) {
    MutableFudgeMsg msg = context.newMessage();
    
    msg.add("boolean", Boolean.TRUE);
    msg.add("Boolean", Boolean.FALSE);
    msg.add("byte", (byte)5);
    msg.add("Byte", new Byte((byte)5));
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    msg.add("short", shortValue);
    msg.add("Short", new Short(shortValue));
    int intValue = ((int)Short.MAX_VALUE) + 5;
    msg.add("int", intValue);
    msg.add("Integer", new Integer(intValue));
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    msg.add("long", longValue);
    msg.add("Long", new Long(longValue));
    
//    msg.add("float", 0.5f);
//    msg.add("Float", new Float(0.5f));
    msg.add("double", 0.27362);
    msg.add("Double", new Double(0.27362));
    
    msg.add("String", "Kirk Wylie");
    
//    msg.add("float array", new float[24]);
    double[] doubleArr = new double[273];
    Arrays.fill(doubleArr, 0.27362);
    msg.add("double array", doubleArr);
//    msg.add("short array", new short[32]);
    int[] intArr = new int[83];
    Arrays.fill(intArr, intValue);
    msg.add("int array", intArr);
    long[] longArr = new long[83];
    Arrays.fill(longArr, longValue);
    msg.add("long array", longArr);
    msg.add("indicator", IndicatorType.INSTANCE);
    
    for (Integer num : Lists.newArrayList(1, 2, 3)) {
      msg.add("intList", num);
    }
    return msg;
  }
  
  /**
   * @param context [documentation not available]
   * @return [documentation not available]
   */
  public static MutableFudgeMsg createMessageAllOrdinals(FudgeContext context) {
    MutableFudgeMsg msg = context.newMessage();
    
    msg.add(1, Boolean.TRUE);
//    msg.add(2, Boolean.FALSE);
//    msg.add(3, (byte)5);
//    msg.add(4, new Byte((byte)5));
//    short shortValue = ((short)Byte.MAX_VALUE) + 5;
//    msg.add(5, shortValue);
//    msg.add(6, new Short(shortValue));
//    int intValue = ((int)Short.MAX_VALUE) + 5;
//    msg.add(7, intValue);
//    msg.add(8, new Integer(intValue));
//    long longValue = ((long)Integer.MAX_VALUE) + 5;
//    msg.add(9, longValue);
//    msg.add(10, new Long(longValue));
//    
//    msg.add(11, 0.5f);
//    msg.add(12, new Float(0.5f));
//    msg.add(13, 0.27362);
//    msg.add(14, new Double(0.27362));
//    
//    msg.add(15, "Kirk Wylie");
//    
//    msg.add(16, new float[24]);
//    msg.add(17, new double[273]);
    
    return msg;
  }
  
  /**
   * @param context [documentation not available]
   * @return [documentation not available]
   */
  public static MutableFudgeMsg createMessageNoNamesNoOrdinals(FudgeContext context) {
    MutableFudgeMsg msg = context.newMessage();
    
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.BOOLEAN, Boolean.TRUE));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.BOOLEAN, Boolean.FALSE));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.BYTE, (byte)5));
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.BYTE, new Byte((byte)5)));
    
    short shortValue = ((short)Byte.MAX_VALUE) + 5;
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.SHORT, shortValue));
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.SHORT, new Short(shortValue)));
    
    int intValue = ((int)Short.MAX_VALUE) + 5;
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.INT, intValue));
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.INT, new Integer(intValue)));
    
    long longValue = ((long)Integer.MAX_VALUE) + 5;
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.LONG, longValue));
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.LONG, new Long(longValue)));
    
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.FLOAT, 0.5f));
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.FLOAT, new Float(0.5f)));
    
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.DOUBLE, 0.27362));
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.DOUBLE, new Double(0.27362)));
    
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.STRING, "kirk Wylie"));
    
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.FLOAT_ARRAY, new float[24]));
//    msg.add(UnmodifiableFudgeField.of(FudgeWireType.DOUBLE_ARRAY, new double[273]));
    return msg;
  }
  
  
  
  /**
   * @param context [documentation not available]
   * @return [documentation not available]
   */
  public static FudgeMsg createMessageAllByteArrayLengths(FudgeContext context) {
    MutableFudgeMsg msg = context.newMessage();
    msg.add("byte[4]", new byte[4]);
    msg.add("byte[8]", new byte[8]);
    msg.add("byte[16]", new byte[16]);
    msg.add("byte[20]", new byte[20]);
    msg.add("byte[32]", new byte[32]);
    msg.add("byte[64]", new byte[64]);
    msg.add("byte[128]", new byte[128]);
    msg.add("byte[256]", new byte[256]);
    msg.add("byte[512]", new byte[512]);
    
    msg.add("byte[28]", new byte[28]);
    return msg;
  }
  
  
  
  /**
   * @param context [documentation not available]
   * @return [documentation not available]
   */
  public static FudgeMsg createMessageWithSubMsgs(FudgeContext context) {
    MutableFudgeMsg msg = context.newMessage();
    MutableFudgeMsg sub1 = context.newMessage();
    sub1.add("bibble", "fibble");
    sub1.add(827, "Blibble");
    MutableFudgeMsg sub2 = context.newMessage();
    sub2.add("bibble9", 9837438);
    sub2.add(828, 82.77f);
    msg.add("sub1", sub1);
    msg.add("sub2", sub2);
    
    return msg;
  }

}
