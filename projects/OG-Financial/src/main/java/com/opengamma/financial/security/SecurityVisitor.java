/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

/**
 * Visitor for the {@code CommodityForwardSecurity} type
 * 
 * @param <C> the type of the security
 * @param <T> visitor method return type
 */
public interface SecurityVisitor<T, C> {

  T accept(C security);
  
}
