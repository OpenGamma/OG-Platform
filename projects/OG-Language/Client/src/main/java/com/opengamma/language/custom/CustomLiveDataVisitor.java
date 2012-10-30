/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.custom;

import com.opengamma.language.livedata.Custom;

/**
 * Defines a visitor pattern for custom live-data messages
 *
 * @param <M>   the message class
 * @param <T1>  the return type
 * @param <T2>  the data type
 */
public interface CustomLiveDataVisitor<M extends Custom, T1, T2> {

  T1 visit(M message, T2 data);

}
