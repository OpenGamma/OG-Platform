/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_blocks.Scenario',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var Block = og.common.util.ui.Block, data, constructor = this;
        var Scenario = function (config) {
            var block = this, id = og.common.id('scenario'), form = config.form,
                index = config.index, extras = {id : id},
                template = '<select class="og-scenario-select" id="'
                + index + '"><option value="">Please select...</option></select>';
            data = config.data;
            form.Block.call(block, {
                extras: extras,
                module: 'og.views.forms.view-definition-scenario_tash',
                children: [
                    new form.Block({content: template})
                ]
            });
            form.on('form:load', function () {
                constructor.content();
            });
        };
        constructor.content = function () {
            og.api.rest.configs.get({type: 'ScenarioDefinition'}).pipe(function (result) {
                $('.og-scenario-select').append('<optgroup label="Scenario Definition"></optgroup>');
                result.data.data.map(function (datum) { // search results
                    var fields = datum.split('|');
                    $('.og-scenario-select optgroup[label="Scenario Definition"]').append(
                        $('<option></option>').val(fields[0]).html(fields[1])
                            .attr('selected', fields[0] === data)
                    );
                });
            });
            og.api.rest.configs.get({type: 'ScenarioDslScript'}).pipe(function (result) {
                $('.og-scenario-select').append('<optgroup label="Scenario DSL Script"></optgroup>');
                result.data.data.map(function (datum) { // search results
                    var fields = datum.split('|');
                    $('.og-scenario-select optgroup[label="Scenario DSL Script"]').append(
                        $('<option></option>').val(fields[0]).html(fields[1])
                            .attr('selected', fields[0] === data)
                    );
                });
            });
        };
        Scenario.prototype = new Block(); // inherit Block prototype
        return Scenario;
    }
});