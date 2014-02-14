<#escape x as x?html>
<@page title="LegalEntity - ${legalEntityDoc.name}">

<@section css="info" if=deleted>
  <p>This legal entity has been deleted</p>
</@section>


<#-- SECTION LegalEntity output -->
<@section title="LegalEntity">
  <p>
    <@rowout label="Name">${legalEntityDoc.name}</@rowout>
    <@rowout label="Reference">${legalEntityDoc.uniqueId.value}, version ${legalEntityDoc.uniqueId.version}, <a href="${uris.legalEntityVersions()}">view history</a>
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail" if=deleted>
    <@rowout label="Data"><textarea readonly style="width:650px;height:300px;">${legalEntityXML}</textarea></@rowout>
</@subsection>
</@section>


<#-- SECTION Update legal entity -->
<@section title="Update legal entity" if=!deleted>
  <@form method="PUT" action="${uris.legalEntity()}" id="updateForm">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${legalEntityDoc.name}" /></@rowin>
    <@rowin>
      <div id="ace-xml-editor">${legalEntityXML}</div>
    </@rowin>
    <input type="hidden" name="legalEntityXML" id="legalEntity-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
<script type="text/javascript">
var editor = ace.edit("ace-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#ace-xml-editor").show()

$("#updateForm").submit( function(eventObj) {
  $("#legalEntity-xml").val(editor.getSession().getValue())
  return true
})
</script>
  </p>
  </@form>
</@section>


<#-- SECTION Delete legal entity -->
<@section title="Delete legal entity" if=!deleted>
  <@form method="DELETE" action="${uris.legalEntity()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.legalEntityVersions()}">History of this legal entity</a><br />
    <a href="${uris.legalEntities()}">LegalEntity home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
