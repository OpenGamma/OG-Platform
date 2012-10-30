/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor for the {@link com.opengamma.financial.security.option.CommodityFutureOptionSecurity} subclasses.
 *
 * @param <T> visitor method return type
 */
public interface CommodityFutureOptionSecurityVisitor<T> {

  T visitCommodityFutureOptionSecurity(CommodityFutureOptionSecurity security);

}
