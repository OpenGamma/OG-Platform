/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.util.PublicSPI;

/**
 * A source of users accessed by any OpenGamma application
 * <p>
 * This interface provides a simple view of users as used by most parts of the application.
 * This may be backed by a full-featured user master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface UserSource extends ChangeProvider {

  /**
   * Gets the user account by OpenGamma user name.
   * 
   * @param userName  the user name, not null
   * @return the user account with the specified user name, not null
   * @throws DataNotFoundException if the user could not be found
   * @throws RuntimeException if an error occurs
   */
  UserAccount getAccount(String userName);

}
