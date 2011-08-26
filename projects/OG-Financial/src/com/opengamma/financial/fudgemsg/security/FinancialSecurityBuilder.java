/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg.security;

import java.lang.reflect.Constructor;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import sun.reflect.ReflectionFactory;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.master.fudgemsg.ManageableSecurityBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code FinancialSecurity}.
 */
public class FinancialSecurityBuilder extends AbstractFudgeBuilder {

  public static void buildMessage(FudgeSerializer serializer, FinancialSecurity object, final MutableFudgeMsg msg) {
    ManageableSecurityBuilder.buildMessage(serializer, object, msg);
  }

  public static void buildObject(FudgeDeserializer deserializer, FudgeMsg msg, FinancialSecurity object) {
    ManageableSecurityBuilder.buildObject(deserializer, msg, object);
  }

  public static <T> T backdoorCreateClass(Class<T> clazz) {
    return backdoorCreateClass(clazz, Object.class);
  }

  public static <T> T backdoorCreateClass(Class<T> clazz, Class<? super T> parent) {
    try {
      ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
      Constructor<?> objDef = parent.getDeclaredConstructor();
      Constructor<?> intConstr = rf.newConstructorForSerialization(clazz, objDef);
      return clazz.cast(intConstr.newInstance());
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException("Cannot create object", ex);
    }
  }

}
