<#escape x as x?html>
<@page title="LegalEntity - ${legalEntityDoc.name}" jquery=true aceXmlEditor=true>

<@section css="info" if=deleted>
  <p>This legal entity has been deleted</p>
</@section>


<#-- SECTION LegalEntity output -->
<@section title="LegalEntity">
  <p>
    <@rowout label="Name">${legalEntityDoc.name}</@rowout>
    <@rowout label="Reference">${legalEntityDoc.uniqueId.value}, version ${legalEntityDoc.uniqueId.version}
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <div id="ace-xml-editor">${legalEntityXML}</div>
</@subsection>

<script type="text/javascript">
var editor = ace.edit("ace-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#ace-xml-editor").show()
</script>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.legalEntityVersions()}">All versions</a><br />
    <a href="${uris.legalEntity()}">Latest version - ${latestLegalEntity.uniqueId.version}</a><br />
    <a href="${uris.legalEntities()}">LegalEntity home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
