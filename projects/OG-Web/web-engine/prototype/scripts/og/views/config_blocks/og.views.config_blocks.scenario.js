/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_blocks.Scenario',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Scenario = function (config) {
            var block = this, id = og.common.id('scenario'),
                form = config.form,
                extras = {id : id};
            form.Block.call(block, {
                extras: extras,
                module: 'og.views.forms.view-definition-scenario_tash'
            });
        };
        Scenario.prototype = new Block(); // inherit Block prototype
        return Scenario;
    }
});