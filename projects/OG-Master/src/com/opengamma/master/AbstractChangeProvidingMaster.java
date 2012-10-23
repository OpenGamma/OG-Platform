/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import com.opengamma.core.change.ChangeProvider;

/**
 * A master that supports change events.
 * 
 * @param <D>  the document type managed by the master
 */
public interface AbstractChangeProvidingMaster<D extends AbstractDocument> extends AbstractMaster<D>, ChangeProvider {

}
