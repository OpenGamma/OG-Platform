/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fra;

/**
 * Visitor for the {@code FRASecurity} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface FRASecurityVisitor<T> {

  T visitFRASecurity(FRASecurity security);

}
