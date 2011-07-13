/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.capfloor;

/**
 * Visitor for the {@code CapFloorSecurity} type
 * 
 * @param <T> visitor method return type
 */
public interface CapFloorSecurityVisitor<T> {

  T visitCapFloorSecurity(CapFloorSecurity security);
}
