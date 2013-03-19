/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.json;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.UnmodifiableFudgeField;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

/**
 * StandardFudgeMessages for testing JSON converters in OG-Web
 */
public final class StandardFudgeMessages {

  /**
   * @param context [documentation not available]
   * @return [documentation not available]
   */
  public static MutableFudgeMsg createMessageAllNames(FudgeContext context) {
    MutableFudgeMsg msg = context.newMessage();
    
    msg.add("boolean", Boolean.TRUE);
    msg.add("Boolean", Boolean.FALSE);
    msg.add("byte", (byte) 5);
    msg.add("Byte", new Byte((byte) 5));
    short shortValue = ((short) Byte.MAX_VALUE) + 5;
    msg.add("short", shortValue);
    msg.add("Short", new Short(shortValue));
    int intValue = ((int) Short.MAX_VALUE) + 5;
    msg.add("int", intValue);
    msg.add("Integer", new Integer(intValue));
    long longValue = ((long) Integer.MAX_VALUE) + 5;
    msg.add("long", longValue);
    msg.add("Long", new Long(longValue));
    
    msg.add("float", 0.5f);
    msg.add("Float", new Float(0.5f));
    msg.add("double", 0.27362);
    msg.add("Double", new Double(0.27362));
    
    msg.add("String", "Kirk Wylie");
    
    msg.add("float array", new float[24]);
    msg.add("double array", new double[273]);
    msg.add("short array", new short[32]);
    msg.add("int array", new int[83]);
    msg.add("long array", new long[837]);
    
    msg.add("indicator", IndicatorType.INSTANCE);
    
    return msg;
  }
  
  /**
   * @param context [documentation not available]
   * @return [documentation not available]
   */
  public static MutableFudgeMsg createMessageAllOrdinals(FudgeContext context) {
    MutableFudgeMsg msg = context.newMessage();
    
    msg.add(1, Boolean.TRUE);
    msg.add(2, Boolean.FALSE);
    msg.add(3, (byte) 5);
    msg.add(4, new Byte((byte) 5));
    short shortValue = ((short) Byte.MAX_VALUE) + 5;
    msg.add(5, shortValue);
    msg.add(6, new Short(shortValue));
    int intValue = ((int) Short.MAX_VALUE) + 5;
    msg.add(7, intValue);
    msg.add(8, new Integer(intValue));
    long longValue = ((long) Integer.MAX_VALUE) + 5;
    msg.add(9, longValue);
    msg.add(10, new Long(longValue));
    
    msg.add(11, 0.5f);
    msg.add(12, new Float(0.5f));
    msg.add(13, 0.27362);
    msg.add(14, new Double(0.27362));
    
    msg.add(15, "Kirk Wylie");
    
    msg.add(16, new float[24]);
    msg.add(17, new double[273]);
    
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
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.BYTE, (byte) 5));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.BYTE, new Byte((byte) 5)));
    
    short shortValue = ((short) Byte.MAX_VALUE) + 5;
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.SHORT, shortValue));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.SHORT, new Short(shortValue)));
    
    int intValue = ((int) Short.MAX_VALUE) + 5;
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.INT, intValue));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.INT, new Integer(intValue)));
    
    long longValue = ((long) Integer.MAX_VALUE) + 5;
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.LONG, longValue));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.LONG, new Long(longValue)));
    
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.FLOAT, 0.5f));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.FLOAT, new Float(0.5f)));
    
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.DOUBLE, 0.27362));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.DOUBLE, new Double(0.27362)));
    
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.STRING, "kirk Wylie"));
    
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.FLOAT_ARRAY, new float[24]));
    msg.add(UnmodifiableFudgeField.of(FudgeWireType.DOUBLE_ARRAY, new double[273]));
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
