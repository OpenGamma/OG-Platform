/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.UniqueIdentifiable;


public interface AbstractChangeProvidingMaster<T extends UniqueIdentifiable, D extends AbstractDocument<? extends T>> extends AbstractMaster<T, D>, ChangeProvider {
}

  

