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
      <li><a href="/analytics">View analytics</a></li>
      <li><a href="${portfolioUris.portfolios()}">View and Manage portfolios</a></li>
      <li><a href="${positionUris.positions()}">View and Manage positions</a></li>
      <li><a href="${securityUris.securities()}">View and Manage securities</a></li>
      <li><a href="${conventionUris.conventions()}">View and Manage conventions</a></li>
      <li><a href="${exchangeUris.exchanges()}">View and Manage exchanges</a></li>
      <li><a href="${holidayUris.holidays()}">View and Manage holidays</a></li>
      <li><a href="${regionUris.regions()}">View and Manage regions</a></li>
      <li><a href="${timeseriesUris.allTimeSeries()}">View and Manage time series</a></li>
      <li><a href="${configUris.configs()}">View and Manage configuration</a></li>
      <li><a href="${snapshotUris.snapshots()}">View and Manage market data snapshots</a></li>
      <li><a href="${functionUris.functions()}">View and Manage functions</a></li>
    </ul>
  </p>
</@section>
<#-- SECTION Links -->
<@section title="Links">
  <p>
<#if uris.about()??>
    <a href="${uris.about()}">View information about the system</a><br />
</#if>
<#if uris.components()??>
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
