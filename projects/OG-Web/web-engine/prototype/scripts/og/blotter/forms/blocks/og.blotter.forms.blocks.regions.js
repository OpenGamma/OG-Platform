/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Regions',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Regions = function (config) {
            var block = this, id = og.common.id('regions'), form = config.form, options = [], menu;
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.regions_tash',
                extras: {name: config.name, value: config.value, id: id}
            });
            block.create_autocomplete = function() {
                og.api.rest.blotter.regions.get().pipe(function(result) {
                    var obj = result.data;
                    Object.keys(obj).forEach(function(key) { 
                        options.push(obj[key]);
                    });
                    menu = new og.common.util.ui.AutoCombo({
                        selector: '#' + id,
                        placeholder: 'Select Region',
                        source: options, 
                        id: id, 
                        name: config.name,
                        no_arrow: true,
                        value: config.value
                    });
                    menu.$input.autocomplete('widget');
                });                           
            };
            form.on('form:load', function() {
                block.create_autocomplete();
            });
        };
        Regions.prototype = new Block(); // inherit Block prototype
        return Regions;
    }
});