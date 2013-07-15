<#escape x as x?html>
<@page title="Exchange history  - ${exchange.name}">

<@section css="info" if=deleted>
  <p>This exchange has been deleted</p>
</@section>


<#-- SECTION Exchange output -->
<@section title="Exchange">
  <p>
    <@rowout label="Name">${exchange.name}</@rowout>
    <@rowout label="Reference">${exchange.uniqueId.value}</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="History">
  <@table items=versionsResult.documents empty="No versions" headers=["Name","Reference","Valid from", "Valid to","Actions"]; item>
      <td><a href="${uris.exchangeVersion(item.exchange)}">${item.exchange.name}</a></td>
      <td>${item.uniqueId.value} v${item.uniqueId.version}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.versionToInstant}</td>
      <td><a href="${uris.exchangeVersion(item.exchange)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.exchange()}">Latest version - ${exchange.uniqueId.version}</a><br />
    <a href="${uris.exchanges()}">Exchange search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
