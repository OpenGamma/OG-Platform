/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import javax.time.InstantProvider;

/**
 * Trait for a "source" backed by a "master" that allows the version to be fixed externally.
 */
public interface VersionedSource {

  void setVersionAsOfInstant(InstantProvider versionAsOf);

  // TODO need a remote version of this to support ViewProcessorManager running on a different node to the actual masters and sources

}
