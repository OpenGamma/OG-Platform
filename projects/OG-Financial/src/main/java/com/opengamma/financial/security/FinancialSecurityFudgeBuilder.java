/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.master.security.ManageableSecurityFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code FinancialSecurity}.
 */
public class FinancialSecurityFudgeBuilder extends AbstractFudgeBuilder {

  public static void toFudgeMsg(FudgeSerializer serializer, FinancialSecurity object, final MutableFudgeMsg msg) {
    ManageableSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, FinancialSecurity object) {
    ManageableSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
  }

//  public static <T> T backdoorCreateClass(Class<T> clazz) {
//    return backdoorCreateClass(clazz, Object.class);
//  }
//
//  public static <T> T backdoorCreateClass(Class<T> clazz, Class<? super T> parent) {
//    try {
//      ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
//      Constructor<?> objDef = parent.getDeclaredConstructor();
//      Constructor<?> intConstr = rf.newConstructorForSerialization(clazz, objDef);
//      return clazz.cast(intConstr.newInstance());
//    } catch (RuntimeException ex) {
//      throw ex;
//    } catch (Exception ex) {
//      throw new IllegalStateException("Cannot create object", ex);
//    }
//  }

}
