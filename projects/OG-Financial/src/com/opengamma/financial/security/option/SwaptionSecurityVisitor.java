/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor for the {@link SwaptionSecurity} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface SwaptionSecurityVisitor<T> {

  T visitSwaptionSecurity(SwaptionSecurity swaptionSecurity);

}
