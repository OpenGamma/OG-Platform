/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_blocks.Scenario',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var Block = og.common.util.ui.Block;
        var Scenario = function (config) {
            var block = this, id = og.common.id('scenario'), form = config.form, script = config.script,
                param = config.param, scriptindex = config.scriptindex, paraindex = config.paraindex,
                extras = {id : id};
            form.Block.call(block, {
                extras: extras,
                module: 'og.views.forms.view-definition-scenario_tash',
                children: [
                    new og.common.util.ui.Dropdown({ form: form, resource: 'configs', index: scriptindex,
                        placeholder: 'Please select....',  value: script, fields: [0, 1],
                        rest_options : {type: 'ScenarioDslScript' }
                        }),
                    new og.common.util.ui.Dropdown({ form: form, resource: 'configs', index: paraindex,
                        placeholder: 'Please select....',  value: param, fields: [0, 1],
                        rest_options : {type: 'ScenarioDslParameters' }
                        })
                ],
                processor: function (data) {
                    data.calculationConfiguration.forEach(function (entry) {
                        if (!entry.scenarioId) {
                            delete entry.scenarioId;
                        }
                        if (!entry.scenarioParametersId) {
                            delete entry.scenarioParametersId;
                        }
                    });
                }
            });
        };
        Scenario.prototype = new Block(); // inherit Block prototype
        return Scenario;
    }
});