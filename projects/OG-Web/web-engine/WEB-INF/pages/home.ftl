<#escape x as x?html>
<@page title="Home">
<#-- SECTION Main options -->
<@section title="Home">
  <p>
    Welcome to OpenGamma risk analytics.
  </p>
  <p>
    Please choose one of the following items to view and manage:
    <ul>
      <#macro link text uriObj>
        <#if uriObj?has_content>
          <li><a href="${uriObj.base()}">${text}</a></li>
        </#if>
      </#macro>
      <@link text="Configuration" uriObj=configUris!""/>
      <@link text="Conventions" uriObj=conventionUris!""/>
      <@link text="Exchanges" uriObj=exchangeUris!""/>
      <@link text="Functions" uriObj=functionUris!""/>
      <@link text="Holidays" uriObj=holidayUris!""/>
      <@link text="Legal Entities" uriObj=legalEntityUris!""/>
      <@link text="Portfolios" uriObj=portfolioUris!""/>
      <@link text="Positions" uriObj=positionUris!""/>
      <@link text="Regions" uriObj=regionUris!""/>
      <@link text="Securities" uriObj=securityUris!""/>
      <@link text="Time Series" uriObj=timeseriesUris!""/>
    </ul>
  </p>
</@section>
<#-- SECTION Links -->
<@section title="Links">
  <p>
<#if uris.about()?has_content>
    <a href="${uris.about()}">View information about the system</a><br />
</#if>
<#if uris.components()?has_content>
    <a href="${uris.components()}">View the components</a><br />
</#if>
  </p>
</@section>
<p>
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
<br />
</p>
</@page>
</#escape>
