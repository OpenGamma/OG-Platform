/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_blocks.Scenario',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Scenario = function (config) {
            var block = this, id = og.common.id('scenario'), data_index = config.index, form = config.form,
                value = config.value, data = config.data, $rules, $rule, extras = {
                    id: id, class_value: (data && data[0])
                };
            var processor = function (data) {
                if (!$('#' + id).length) return;
                var indices = data_index.split('.'), last = indices.pop(), result = {};
            };
            form.Block.call(block, {extras: extras, processor: processor}); // assign a Block instance to this (block)
            block.on('form:load', function () {
                
            });
        };
        Scenario.prototype = new Block(null, { // inherit Block prototype
            module: 'og.views.forms.view-definition-scenario_tash'
        });
        return Scenario;
    }
});