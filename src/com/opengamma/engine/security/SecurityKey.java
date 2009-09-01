/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security;

/**
 * A way of defining one or more {@link Security} instances from a set
 * of descriptive data about that security.
 * It is up to the {@link SecurityMaster} implementation to determine
 * a mapping strategy to identify whether a particular key matches
 * a particular {@link Security}.
 *
 * @author kirk
 */
public interface SecurityKey {

}
