/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import com.opengamma.engine.view.listener.ViewResultListener;

/**
 * This interface allows the registration of result listeners that should not be reference counted when deciding whether or not to shut-down a view.
 */
public interface InternalViewResultListener extends ViewResultListener {

}
