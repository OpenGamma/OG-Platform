<#escape x as x?html>
<@page title="${title}">
    <@section title="Security Types">
        <@table items=securityTypeNames empty="" headers=["Type Name"]; typeName>
            <td><a href="${typeName}">${typeName}</a></td>
        </@table>
    </@section>
    <@section title="Other Types">
        <@table items=otherTypeNames empty="" headers=["Type Name"]; typeName>
            <td><a href="${typeName}">${typeName}</a></td>
        </@table>
    </@section>
</@page>
</#escape>
