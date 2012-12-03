/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.Attributes',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Attributes = function (config) {
            var block = this, attr, form = config.form;
            var generator = function (handler) {
                console.log('I generate therefore I am');
            };
            attr = {
                generator: generator, 
                module: 'og.views.forms.attributes_tash', 
                extras: config.extras
            };
            form.Block.call(block, attr);
            block.id = id;
        };
        Attributes.prototype = new Block(); // inherit Block prototype
        /*Attributes.prototype.off = function () {
        };
        Attributes.prototype.on = function () {
        };*/
        return Attributes;
    }
});