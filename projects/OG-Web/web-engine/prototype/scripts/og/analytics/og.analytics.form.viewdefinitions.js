/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.ViewDefinitions',
    dependencies: ['og.common.util.ui.AutoCombo'],
    obj: function () {
        var module = this, menu, Block = og.common.util.ui.Block, viewdefs, viewdefs_store,
            tmpl_header = '<div class="og-option-title"><header class="OG-background-05">calculations:</header></div>';

        var ac_source = function (src, callback) {
            return function (req, res) {
                var escaped = $.ui.autocomplete.escapeRegex(req.term),
                    matcher = new RegExp(escaped, 'i'),
                    htmlize = function (str) {
                        return !req.term ? str : str.replace(
                            new RegExp(
                                '(?![^&;]+;)(?!<[^<>]*)(' + escaped + ')(?![^<>]*>)(?![^&;]+;)', 'gi'
                            ), '<strong>$1</strong>'
                        );
                    };
                src.get({page: '*'}).pipe(function (resp){
                    var data = callback(resp);
                    if (data && data.length) {
                        data.sort((function(){
                            return function (a, b) {return (a === b ? 0 : (a < b ? -1 : 1));};
                        })());
                        res(data.reduce(function (acc, val) {
                            if (!req.term || val && matcher.test(val)) acc.push({label: htmlize(val)});
                            return acc;
                        }, []));
                    } else $('input', '.og-view.og-autocombo').removeClass('ui-autocomplete-loading');
                });
            };
        };

        var store_viewdefinitions = function (resp) {
            return viewdefs = (viewdefs_store = resp.data).pluck('name');
        };

        var ViewDefinitions = function (config, callback) {
            var block = this, menu, form = config.form;
            form.Block.call(block, {
                content: tmpl_header,
                processor: function (data) {
                    var vw, vd = viewdefs_store.filter(function (entry) {
                            return entry.name === menu.$input.val();
                        });
                    if (vd && vd.length && vd[0].id) vw = vd[0].id;
                    else vw = menu.$input.val();
                    data.viewdefinition = vw;
                }
            });
            form.on("form:load", function () {
                menu = new og.common.util.ui.AutoCombo({
                    selector: '.og-view.og-autocombo',
                    placeholder: 'Search...',
                    source: ac_source(og.api.rest.viewdefinitions, store_viewdefinitions)
                });
                menu.$input.on('keydown', function (event) {
                    if (event.keyCode === $.ui.keyCode.ENTER) form.submit();
                });
                if (config.val) {
                    og.api.rest.viewdefinitions.get().pipe(function (resp) {
                        store_viewdefinitions(resp);
                        var val = viewdefs_store.filter(function (entry) { return entry.id === config.val; });
                        if (val.length && val[0].name) menu.$input.val(val[0].name);
                    });
                }
            })
        };

        ViewDefinitions.prototype = new Block;
        return ViewDefinitions;
    }
});