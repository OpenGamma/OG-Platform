/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.custom;

/**
 * Ability to register custom top-level message visitors.
 * 
 * @param <T1> visitor return type
 * @param <T2> visitor data
 */
public interface CustomMessageVisitorRegistry<T1, T2> {

  <M extends com.opengamma.language.connector.Custom> void register(Class<M> clazz,
      CustomMessageVisitor<M, T1, T2> visitor);

}
