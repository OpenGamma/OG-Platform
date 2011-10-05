package com.opengamma.language.object;

import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.language.function.MetaFunction;
import org.testng.annotations.Test;

import javax.time.calendar.ZonedDateTime;

import java.rmi.server.UID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class CreateBeanFunctionTest {

  @Test
  public void invoke() {
    String testFunctionName = "testFunctionName";
    CreateBeanFunction<FXForwardSecurity> function =
        new CreateBeanFunction<FXForwardSecurity>(testFunctionName, FXForwardSecurity.class);

    UniqueId uniqueId = UniqueId.of("Tst", "uid");
    ExternalId regionId = ExternalId.of("Tst", "region");
    ExternalId underlyingId = ExternalId.of("Tst", "underlying");
    ZonedDateTime forwardDate = ZonedDateTime.now();
    ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("Tst", "id"));
    String name = "securityName";
    String securityType = "securityType";
    Object[] parameters = {uniqueId, idBundle, name, securityType, underlyingId, forwardDate, regionId};

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
    assertEquals(underlyingId, security.getUnderlyingId());
    assertEquals(forwardDate, security.getForwardDate());
    assertEquals(regionId, security.getRegionId());
  }
}
