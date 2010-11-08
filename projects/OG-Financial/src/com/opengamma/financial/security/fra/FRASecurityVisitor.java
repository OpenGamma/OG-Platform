/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
