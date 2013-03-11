<#escape x as x?html>
<@page title="Holiday - ${holidayDoc.name}">

<@section css="info" if=deleted>
  <p>This holiday has been deleted</p>
</@section>


<#-- SECTION Holiday output -->
<@section title="Holiday">
  <p>
    <@rowout label="Name">${holidayDoc.name}</@rowout>
    <@rowout label="Reference">${holidayDoc.uniqueId.value}, version ${holidayDoc.uniqueId.version}<#--, <a href="${uris.holidayVersions()}">view history</a>-->
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
<#if holidayDoc.providerId?has_content>
    <@rowout label="Provider id">${holidayDoc.providerId.scheme.name} - ${holidayDoc.providerId.value}</@rowout>
</#if>
    <@rowout label="Type">${holiday.type}</@rowout>
<#if holiday.currencyISO?has_content>
    <@rowout label="Currency">${holiday.currencyISO} <a href="${regionUris.regionsByCurrency(holiday.currencyISO)}">view</a></@rowout>
</#if>
<#if holiday.regionId?has_content>
    <@rowout label="Region">${holiday.regionId.scheme.name} - ${holiday.regionId.value} <a href="${regionUris.regions(holiday.regionId)}">view</a></@rowout>
</#if>
<#if holiday.exchangeId?has_content>
    <@rowout label="Exchange">${holiday.exchangeId.scheme.name} - ${holiday.exchangeId.value} <a href="${exchangeUris.exchanges(holiday.exchangeId)}">view</a></@rowout>
</#if>
</@subsection>

<#-- SUBSECTION Dates -->
<@subsection title="Dates">
<@table items=holidayDatesByYear empty="No dates" headers=["Year","Dates"]; item>
      <td>${item.first.value?c}</td>
      <td>
<#list item.second as date>
      ${date},
</#list>
      </td>
</@table>
</@subsection>
</@section>


<#-- SECTION Update holiday >
<@section title="Update holiday" if=!deleted>
  <@form method="PUT" action="${uris.holiday()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin label="Scheme type"><input type="text" size="30" maxlength="80" name="idscheme" value="" /></@rowin>
    <@rowin label="Scheme id"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <@rowin label="Region type"><input type="text" size="30" maxlength="80" name="regionscheme" value="" /></@rowin>
    <@rowin label="Region id"><input type="text" size="30" maxlength="80" name="regionvalue" value="" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section-->


<#-- SECTION Delete holiday >
<@section title="Delete holiday" if=!deleted>
  <@form method="DELETE" action="${uris.holiday()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section-->


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <#--a href="${uris.holidayVersions()}">History of this holiday</a><br /-->
    <a href="${uris.holidays()}">Holiday search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
