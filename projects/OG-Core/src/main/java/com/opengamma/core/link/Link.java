package com.opengamma.core.link;

import com.opengamma.DataNotFoundException;

/**
 * Created by julian on 16/04/14.
 */
public interface Link<T> {

  /**
   * Resolve the link and get the underlying object.
   *
   * @return the target of the link, not null
   * @throws DataNotFoundException if the link is not resolvable
   */
  T resolve();
}
