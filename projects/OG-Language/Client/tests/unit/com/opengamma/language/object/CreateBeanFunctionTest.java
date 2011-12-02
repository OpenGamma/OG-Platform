package com.opengamma.language.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.language.function.MetaFunction;

public class CreateBeanFunctionTest {

  /**
   * This is needed to register the meta-bean for the security type so it can be looked up when the bean is created.
   */
  @BeforeClass
  public void registerMetaBean() throws ClassNotFoundException {
    Class.forName("com.opengamma.financial.security.fx.FXForwardSecurity");
  }

  @Test
  public void invoke() {
    String testFunctionName = "testFunctionName";
    CreateBeanFunction function = new CreateBeanFunction(testFunctionName, FXForwardSecurity.class);

    UniqueId uniqueId = UniqueId.of("Tst", "uid");
    ExternalId regionId = ExternalId.of("Tst", "region");
    ExternalId underlyingId = ExternalId.of("Tst", "underlying");
    ZonedDateTime forwardDate = ZonedDateTime.now();
    ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("Tst", "id"));
    String name = "securityName";
    String securityType = "securityType";
    Map<String, String> attributes = Collections.singletonMap("Foo", "Bar");
    Object[] parameters = {uniqueId, idBundle, name, securityType, attributes, underlyingId, forwardDate, regionId };

    MetaFunction metaFunction = function.getMetaFunction();
    assertNotNull(metaFunction);
    assertEquals(testFunctionName, metaFunction.getName());
    CreateBeanFunction.Invoker invoker = (CreateBeanFunction.Invoker) metaFunction.getInvoker();
    Object bean = invoker.invokeImpl(null, parameters);
    assertNotNull(bean);
    assertTrue(bean instanceof FXForwardSecurity);
    FXForwardSecurity security = (FXForwardSecurity) bean;
    assertEquals(idBundle, security.getExternalIdBundle());
    assertEquals(name, security.getName());
    assertEquals(securityType, security.getSecurityType());
    assertEquals(attributes, security.getAttributes());
    assertEquals(underlyingId, security.getUnderlyingId());
    assertEquals(forwardDate, security.getForwardDate());
    assertEquals(regionId, security.getRegionId());
  }
}
