<#escape x as x?html>
<@page title="Batches">


<#-- SECTION Batch search -->
<@section title="Batch search" if=searchRequest??>
  <@form method="GET" action="${uris.batches()}">
  <p>
  	<@rowin label="Observation date"><input type="text" size="30" maxlength="80" name="observationDate" value="${searchRequest.observationDate}" /></@rowin>
    <@rowin label="Observation time"><input type="text" size="30" maxlength="80" name="observationTime" value="${searchRequest.observationTime}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Batch results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No batches" headers=["Observation date","Observation time","Status","Actions"]; item>
      <td>${item.observationDate}</td>
      <td>${item.observationTime}</td>
      <td>${item.status}</td>
      <td><a href="${uris.batch(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
