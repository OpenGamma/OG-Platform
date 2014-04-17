/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

public interface TargetableLink<T> extends Link<T> {

  /**
   * Create a new Link, with the same name and type as this one that uses a newly
   * provided serviceContext. This should only be necessary when you need to use resolution
   * outside of the current VersionCorrection threadlocal environment.
   *
   * VersionCorrectionProvider necessary to resolve
   * @return a new link
   */
  Link<T> withTargetType(Class<T> targetType);

}
