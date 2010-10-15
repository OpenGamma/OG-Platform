/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.cache.msg;

/**
 * Visitor to the {@link BinaryDataStoreResponse} message sub-classes.
 */
public abstract class BinaryDataStoreResponseVisitor {

  protected abstract void visitUnexpectedMessage(BinaryDataStoreResponse message);

}
