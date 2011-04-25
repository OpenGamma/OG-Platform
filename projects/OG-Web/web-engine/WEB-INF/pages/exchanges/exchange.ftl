<#escape x as x?html>
<@page title="Exchange - ${exchange.name}">

<@section css="info" if=deleted>
  <p>This exchange has been deleted</p>
</@section>


<#-- SECTION Exchange output -->
<@section title="Exchange">
  <p>
    <@rowout label="Name">${exchange.name}</@rowout>
    <@rowout label="Reference">${exchange.uniqueId.value}, version ${exchange.uniqueId.version}, <a href="${uris.exchangeVersions()}">view history</a>
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
<#list exchange.regionKey.identifiers as item>
    <@rowout label="Region id">${item.scheme.name} - ${item.value}</@rowout>
</#list>
<#list exchange.identifiers.identifiers as item>
    <@rowout label="Key">${item.scheme.name} - ${item.value}</@rowout>
</#list>
</@subsection>
</@section>


<#-- SECTION Update exchange -->
<@section title="Update exchange" if=!deleted>
  <@form method="PUT" action="${uris.exchange()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin label="Scheme type"><input type="text" size="30" maxlength="80" name="idscheme" value="" /></@rowin>
    <@rowin label="Scheme id"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <@rowin label="Region type"><input type="text" size="30" maxlength="80" name="regionscheme" value="" /></@rowin>
    <@rowin label="Region id"><input type="text" size="30" maxlength="80" name="regionvalue" value="" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Delete exchange -->
<@section title="Delete exchange" if=!deleted>
  <@form method="DELETE" action="${uris.exchange()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.exchangeVersions()}">History of this exchange</a><br />
    <a href="${uris.exchanges()}">Exchange search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
