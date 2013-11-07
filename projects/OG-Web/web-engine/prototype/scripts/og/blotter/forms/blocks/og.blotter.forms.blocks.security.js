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
            var block = this, scheme_value, id_value, form = config.form, security = config.security, dropdown,
                sec_id = og.common.id("og-blotter-security"), scheme_id = og.common.id("og-blotter-scheme"),
                scheme_selector =  og.common.id("og-blotter-scheme-selector"), options = [], menu,
                scheme_tmpl = '<span id="' + scheme_selector + '"></span>', callback = config.callback || $.noop;
            if (security) {
                scheme_value = security.split(/~(.+)/)[0];
                id_value  = security.split(/~(.+)/)[1];
            }
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.security_tash',
                extras: {label: config.label, sec_id: sec_id, value: id_value, disabled: !!config.edit},
                children: [ new form.Block({content: scheme_tmpl})],
                processor: function (data) {
                    if (!config.edit) {
                        var path = config.index.split('.'), last = path.pop(),
                            merge = data[scheme_id].trim() + "~" + data[sec_id].trim();
                        path.reduce(function (acc, val) {return acc[val]; }, data)[last] = merge;
                    }
                    delete data[sec_id];
                    delete data[scheme_id];
                }
            });
            block.name = function () {
                var scheme = $('#' + scheme_id).val().trim(), id = $('#' + sec_id).val().trim();
                if (!scheme.length || !id.length) {
                    return false;
                }
                return scheme + '~' + id;
            };
            block.create_autocomplete = function () {
                og.api.rest.blotter.idschemes.get({cache_for: 60000}).pipe(function (result) {
                    var obj = result.data;
                    Object.keys(obj).forEach(function (key) {
                        options.push(obj[key]);
                    });
                    options.sort();
                    menu = new og.common.util.ui.AutoCombo({
                        selector: '#' + scheme_selector,
                        placeholder: 'Select Scheme',
                        source: options,
                        id: scheme_id,
                        name: scheme_id,
                        no_arrow: true,
                        select: function (event, ui) {
                            $('#' + scheme_id).val(ui.item.value);
                            callback();
                        },
                        value: scheme_value,
                        disabled: config.edit
                    });
                    menu.$input.autocomplete('widget').css('max-height', 500);
                    form.on('keyup', '#' + sec_id, function (event) {callback(); });
                    form.on('keyup', '#' + scheme_id, function (event) {callback(); });
                    callback();
                });
            };
            form.on('form:load', function () {
                block.create_autocomplete();
            });
        };
        Security.prototype = new Block(); // inherit Block prototype
        return Security;
    }
});