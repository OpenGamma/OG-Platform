/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.fx;

/**
 * Visitor for the {@code NonDeliverableFXForwardSecurity} type
 * 
 * @param <T> visitor method return type
 */
public interface NonDeliverableFXForwardSecurityVisitor<T> {

  T visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security);
}
