<#escape x as x?html>
<@page title="Security - ${security.name}">

<@section css="info" if=deleted>
  <p>This security has been deleted</p>
</@section>


<#-- SECTION Security output -->
<@section title="Security">
  <p>
    <@rowout label="Name">${security.name}</@rowout>
    <@rowout label="Reference">${security.uniqueId.value}, version ${security.uniqueId.version}</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <@rowout label="Type">${security.securityType}</@rowout>
<#list security.externalIdBundle.externalIds as item>
    <@rowout label="Key">${item.scheme.name} - ${item.value}</@rowout>
</#list>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.securityVersions()}">All versions</a><br />
    <a href="${uris.security()}">Latest version - ${latestSecurity.uniqueId.version}</a><br />
    <a href="${uris.securities()}">Security search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
