<#escape x as x?html>
<@page title="Exchange - ${exchange.name}">

<@section css="info" if=deleted>
  <p>This exchange has been deleted</p>
</@section>


<#-- SECTION Exchange output -->
<@section title="Exchange">
  <p>
    <@rowout label="Name">${exchange.name}</@rowout>
    <@rowout label="Reference">${exchange.uniqueId.value}, version ${exchange.uniqueId.version}
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
<#if exchange.regionIdBundle?has_content>
<#list exchange.regionIdBundle.externalIds as item>
    <@rowout label="Region id">${item.scheme.name} - ${item.value}</@rowout>
</#list>
</#if>
<#list exchange.externalIdBundle.externalIds as item>
    <@rowout label="Key">${item.scheme.name} - ${item.value}</@rowout>
</#list>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.exchangeVersions()}">All versions</a><br />
    <a href="${uris.exchange()}">Latest version - ${latestExchange.uniqueId.version}</a><br />
    <a href="${uris.exchanges()}">Exchange search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
