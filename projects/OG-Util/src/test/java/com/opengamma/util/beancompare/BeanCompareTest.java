/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.beancompare;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class BeanCompareTest {

  private static final String UNIQUE_ID = "uniqueId";
  private static final String EXTERNAL_ID_BUNDLE = "externalIdBundle";
  private static final String NAME = "name";

  @Test
  @SuppressWarnings("deprecation")
  public void equalIgnoring() {
    UniqueId uid1 = UniqueId.of("uid", "123");
    UniqueId uid2 = UniqueId.of("uid", "124");
    ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    Bean bean1 = createBean(uid1, eid1, "name1");
    Bean bean2 = createBean(uid2, eid2, "name1");
    assertFalse(BeanCompare.equalIgnoring(bean1, bean2));
    assertTrue(BeanCompare.equalIgnoring(bean1, bean2, bean1.metaBean().metaProperty(UNIQUE_ID)));
  }

  @Test
  public void propertyComparators_same() {
    UniqueId uid1 = UniqueId.of("uid", "123");
    UniqueId uid2 = UniqueId.of("uid", "123");
    ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    Bean bean1 = createBean(uid1, eid1, "name1");
    Bean bean2 = createBean(uid2, eid2, "name1");
    BeanCompare beanCompare = new BeanCompare();
    List<BeanDifference<?>> diff = beanCompare.compare(bean1, bean2);
    assertTrue(diff.isEmpty());
  }

  @Test
  public void propertyComparators_different() {
    UniqueId uid1 = UniqueId.of("uid", "123");
    UniqueId uid2 = UniqueId.of("uid", "123");
    ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid2", "abc"));
    Bean bean1 = createBean(uid1, eid1, "name1");
    Bean bean2 = createBean(uid2, eid2, "name1");
    BeanCompare beanCompare = new BeanCompare();
    List<BeanDifference<?>> diff = beanCompare.compare(bean1, bean2);
    assertFalse(diff.isEmpty());
  }

  @Test
  public void propertyComparators_ignoreDifferences() {
    Comparator<Object> alwaysEqualComparator = new Comparator<Object>() {
      @Override
      public int compare(Object notUsed1, Object notUsed2) {
        return 0;
      }
    };
    UniqueId uid1 = UniqueId.of("uid", "123");
    UniqueId uid2 = UniqueId.of("uid", "321");
    ExternalIdBundle eid1 = ExternalIdBundle.of(ExternalId.of("eid1", "321"));
    ExternalIdBundle eid2 = ExternalIdBundle.of(ExternalId.of("eid2", "abc"));
    Bean bean1 = createBean(uid1, eid1, "name1");
    Bean bean2 = createBean(uid2, eid2, "name1");
    MetaProperty<Object> uniqueIdMeta = bean1.property(UNIQUE_ID).metaProperty();
    MetaProperty<Object> externalIdMeta = bean1.property(EXTERNAL_ID_BUNDLE).metaProperty();
    Map<MetaProperty<?>, Comparator<Object>> comparators =
        ImmutableMap.<MetaProperty<?>, Comparator<Object>>of(
            uniqueIdMeta, alwaysEqualComparator,
            externalIdMeta, alwaysEqualComparator);
    BeanCompare beanCompare = new BeanCompare(comparators, Collections.<Class<?>, Comparator<Object>>emptyMap());
    // same despite different IDs
    List<BeanDifference<?>> diff = beanCompare.compare(bean1, bean2);
    assertTrue(diff.toString(), diff.isEmpty());
  }

  private static Bean createBean(UniqueId uniqueId, ExternalIdBundle idBundle, String name) {
    FlexiBean bean = new FlexiBean();
    bean.propertyDefine(UNIQUE_ID, UniqueId.class);
    bean.propertyDefine(EXTERNAL_ID_BUNDLE, ExternalIdBundle.class);
    bean.propertyDefine(NAME, String.class);
    bean.propertySet(UNIQUE_ID, uniqueId);
    bean.propertySet(EXTERNAL_ID_BUNDLE, idBundle);
    bean.propertySet(NAME, name);
    return bean;
  }

}
