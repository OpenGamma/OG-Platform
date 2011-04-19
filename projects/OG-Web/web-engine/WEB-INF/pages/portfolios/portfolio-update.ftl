<#escape x as x?html>
<@page title="Update portfolio - ${portfolio.name}">


<#-- SECTION Portfolio output -->
<@section title="Portfolio">
  <p>
    <@rowout label="Name">${portfolio.name}</@rowout>
    <@rowout label="Reference">${portfolio.uniqueId.value}, version ${portfolio.uniqueId.version}</@rowout>
  </p>
</@section>


<#-- SECTION Update portfolio -->
<@section title="Update portfolio">
  <@form method="PUT" action="${uris.portfolio()}">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${portfolio.name}" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.portfolio()}">Portfolio - ${portfolio.name}</a><br />
    <a href="${uris.portfolios()}">Portfolio search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
