/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.cache.msg;

/**
 * Visitor to the {@link BinaryDataStoreRequest} message sub-classes.
 */
public abstract class BinaryDataStoreRequestVisitor {

  protected abstract void visitUnexpectedMessage(BinaryDataStoreRequest message);

}
