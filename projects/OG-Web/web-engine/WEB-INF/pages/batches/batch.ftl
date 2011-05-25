<#escape x as x?html>
<@page title="Batches - ${batch.observationDate}/${batch.observationTime}">

<#-- SECTION batch output -->
<@section title="Batch">
  <p>
    <@rowout label="Observation date">${batch.observationDate}</@rowout>
    <@rowout label="Observation time">${batch.observationTime}</@rowout>
    <@rowout label="Status">${batch.status}</@rowout>
    <@rowout label="Creation instant">${batch.creationInstant}</@rowout>
    <@rowout label="Start instant">${batch.startInstant}</@rowout>
    <@rowout label="End instant">${batch.endInstant}</@rowout>
    <@rowout label="Main server">${batch.masterProcessHost}</@rowout>
    <@rowout label="Number of restarts">${batch.numRestarts}</@rowout>
    <@rowout label="Total results">${resultPaging.totalItems}</@rowout>
    <@rowout label="Total errors">${errorPaging.totalItems}</@rowout>
  </p>
<@subsection title="Results">
<@table items=batch.data paging=resultPaging empty="No results" headers=
	["Calculation configuration",
	"Computation target",
	"Value name",
	"Function unique id",
	"Value"
	]; item>
      <td>${item.calculationConfiguration}</td>
      <td>${item.computedValue.specification.targetSpecification.uniqueId}</td>
      <td>${item.computedValue.specification.valueName}</td>
      <td>${item.computedValue.specification.functionUniqueId}</td>
      <td>${item.computedValue.value}</td>
</@table>
</@subsection>
<@subsection title="Errors">
<@table items=batch.errors paging=errorPaging empty="No errors" headers=
	["Calculation configuration",
	"Computation target",
	"Value name",
	"Function unique id",
	"Exception class",
	"Exception message",
	"Stack trace"
	]; item>
      <td>${item.calculationConfiguration}</td>
      <td>${item.computationTarget}</td>
      <td>${item.valueName}</td>
      <td>${item.functionUniqueId}</td>
      <td>${item.exceptionClass}</td>
      <td>${item.exceptionMsg}</td>
      <td>${item.stackTrace}</td>
</@table>
</@subsection>
<@subsection title="Export">
<a href="${uris.batch() + ".csv?export=results"}">Download results as CSV</a>
<p/>
<a href="${uris.batch() + ".csv?export=errors"}">Download errors as CSV</a>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.batches()}">Batch search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
