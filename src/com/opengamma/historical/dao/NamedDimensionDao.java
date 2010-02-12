/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.historical.dao;

import java.util.Set;

/**
 * 
 *
 * @author yomi
 */
public interface NamedDimensionDao {
  public Set<NamedDimensionDao> getAll();
  public Set<String> getAllNames();
  public Set<String> getById();
}
