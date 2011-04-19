<#escape x as x?html>
<@page title="Position history  - ${position.name}">

<@section css="info" if=deleted>
  <p>This position has been deleted</p>
</@section>


<#-- SECTION Position output -->
<@section title="Position">
  <p>
    <@rowout label="Name">${position.name}</@rowout>
    <@rowout label="Reference">${position.uniqueId.value}</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="History">
  <@table items=versionsResult.documents empty="No versions" headers=["Name","Reference","Valid from", "Valid to","Quantity","Trades","Actions"]; item>
      <td><a href="${uris.positionVersion(item.position)}">${item.position.name}</a></td>
      <td>${item.uniqueId.value} v${item.uniqueId.version}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.versionToInstant}</td>
      <td>${item.position.quantity}</td>
      <td>${item.position.trades?size}</td>
      <td><a href="${uris.positionVersion(item.position)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.position()}">Latest version - ${position.uniqueId.version}</a><br />
    <a href="${uris.positions()}">Position search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
