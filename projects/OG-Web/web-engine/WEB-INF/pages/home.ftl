<#escape x as x?html>
<@page title="Home">
<#-- SECTION Main options -->
<@section title="Home">
  <p>
    Welcome to OpenGamma risk analytics.
  </p>
  <p>
    Please choose one of the following options:
    <ul>
      <#macro link text uriObj>
        <#if uriObj?has_content>
          <li><a href="${uriObj.base()}">${text}</a></li>
        </#if>
      </#macro>
      <@link text="View and Manage portfolios" uriObj=portfolioUris!""/>
      <@link text="View and Manage positions" uriObj=positionUris!""/>
      <@link text="View and Manage securities" uriObj=securityUris!""/>
      <@link text="View and Manage conventions" uriObj=conventionUris!""/>
      <@link text="View and Manage legal entities" uriObj=legalEntityUris!""/>
      <@link text="View and Manage exchanges" uriObj=exchangeUris!""/>
      <@link text="View and Manage holidays" uriObj=holidayUris!""/>
      <@link text="View and Manage regions" uriObj=regionUris!""/>
      <@link text="View and Manage time series" uriObj=timeseriesUris!""/>
      <@link text="View and Manage configuration" uriObj=configUris!""/>
      <@link text="View and Manage functions" uriObj=functionUris!""/>
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
