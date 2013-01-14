/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Security',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Security = function (config) {
            var block = this, scheme_value, id_value, form = config.form, security = config.security,
                sec_id = og.common.id("og-blotter-security");
            if(security){
                scheme_value = security.split(/~(.+)/)[0];
                id_value  = security.split(/~(.+)/)[1]; 
            }
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.security_tash', 
                extras: {label: config.label, sec_id: sec_id, value: id_value},
                children: [
                    new og.common.util.ui.Dropdown({
                        form: form, resource: 'blotter.idschemes', index: config.index,
                        value: "scheme_value", placeholder: 'Select Scheme',
                        data_generator: function (handler) {
                            og.api.rest.blotter.idschemes.get().pipe(function (result){
                                var options = [], obj = result.data;
                                Object.keys(obj).forEach(function(key) { 
                                    options.push(obj[key]);
                                });
                                handler(options);
                            });
                        }
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