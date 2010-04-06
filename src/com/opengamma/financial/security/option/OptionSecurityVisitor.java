/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

public interface OptionSecurityVisitor<T> extends ExchangeTradedOptionSecurityVisitor<T>, OTCOptionSecurityVisitor<T> {
  
}