/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.livedata;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.util.ArgumentChecker;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Produce a {@link LiveDataProvider} from a set of {@link PublishedLiveData} objects.
 */
public class LiveDataProviderBean extends AbstractLiveDataProvider implements InitializingBean {

  private Collection<PublishedLiveData> _liveData;

  public void setLiveData(final Collection<PublishedLiveData> liveData) {
    ArgumentChecker.notNull(liveData, "liveData");
    _liveData = new ArrayList<PublishedLiveData>(liveData);
  }

  private Collection<PublishedLiveData> getLiveDataInternal() {
    return _liveData;
  }

  @SuppressWarnings("unchecked")
  public Collection<PublishedLiveData> getLiveData() {
    return Collections.unmodifiableCollection(getLiveDataInternal());
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getLiveDataInternal(), "liveData");
  }

  // AbstractLiveDataProvider

  @Override
  protected void loadDefinitions(final Collection<MetaLiveData> definitions) {
    for (PublishedLiveData liveData : getLiveDataInternal()) {
      definitions.add(liveData.getMetaLiveData());
    }
  }

}
