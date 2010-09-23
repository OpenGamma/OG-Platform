/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fra;

public interface FRASecurityVisitor<T> {

  T visitFRASecurity(FRASecurity security);

}
