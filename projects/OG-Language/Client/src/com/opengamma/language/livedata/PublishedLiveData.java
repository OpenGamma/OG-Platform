/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;


/**
 * Marks an object as a live data that can be published to a client.
 */
public interface PublishedLiveData {

  MetaLiveData getMetaLiveData();

}
