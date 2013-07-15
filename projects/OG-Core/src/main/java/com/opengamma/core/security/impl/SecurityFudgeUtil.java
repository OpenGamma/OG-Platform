/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.security.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeObjectReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class SecurityFudgeUtil {
  private SecurityFudgeUtil() {
  }

  public static byte[] convertToFudge(FudgeContext fudgeContext, Security security) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(security, "security");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    fudgeContext.writeObject(security, baos);
    byte[] bytes = baos.toByteArray();
    return bytes;
  }

  public static Security convertFromFudge(FudgeContext fudgeContext, String className, byte[] data) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(className, "className");
    
    if (data == null) {
      return null;
    }
    
    Class<?> clazz = null;
    try {
      //clazz = Class.forName(className);
      clazz = SecurityFudgeUtil.class.getClassLoader().loadClass(className);
    } catch (ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException("No class available with name " + className + " for decoding.", ex);
    }
    
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    FudgeObjectReader objectReader = fudgeContext.createObjectReader(bais);
    Security security = (Security) objectReader.read(clazz);
    return security;
  }

}
