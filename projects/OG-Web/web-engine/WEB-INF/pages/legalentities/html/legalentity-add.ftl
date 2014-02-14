<#escape x as x?html>
<@page title="Add legal entity" jquery=true aceXmlEditor=true>


<#-- SECTION Add legal entity -->
<@section title="Add legal entity">
  <@form method="POST" action="${uris.legalEntities()}" id="addForm">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${name}" /></@rowin>
    
    <#if err_xmlMissing??><div class="err">The data must be entered</div></#if>
    <@rowin>
      <div id="ace-xml-editor">${legalEntityXML}}</div>
    </@rowin>
    
    <input type="hidden" name="legalEntityXML" id="legalEntity-xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
<script type="text/javascript">
var editor = ace.edit("ace-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#ace-xml-editor").show()

$("#addForm").submit( function(eventObj) {
  $("#legalEntity-xml").val(editor.getSession().getValue())
  return true
})
</script>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.legalEntities()}">LegalEntity home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
