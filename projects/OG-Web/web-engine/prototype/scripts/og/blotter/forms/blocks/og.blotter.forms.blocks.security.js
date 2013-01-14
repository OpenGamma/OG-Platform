/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Security',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block,  sec_id = og.common.id("og-blotter-security-");
        var Security = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form;
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.security_tash', 
                extras: {label: config.label, sec_id: sec_id, value: config.id},
                children: [
                    new ui.Dropdown({
                      form: form, resource: 'blotter.idschemes', index: config.index,
                      value: config.scheme, placeholder: 'Select Scheme'
                    })
                ],                       
                processor: function (data) {
                    var str;
                    str = data[config.index] + "~" + data[sec_id];
                    delete data[sec_id];
                    data[config.index] = str;
                }
            });
        };
        Security.prototype = new Block(); // inherit Block prototype
        return Security;
    }
});