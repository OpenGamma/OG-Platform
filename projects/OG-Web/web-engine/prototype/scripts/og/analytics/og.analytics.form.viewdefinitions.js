/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.ViewDefinitions',
    dependencies: ['og.common.util.ui.AutoCombo'],
    obj: function () {
        var module = this, menu, block, viewdefs, viewdefs_store,
            tmpl_header = '<div class="og-option-title"><header class="OG-background-05">calculations:</header></div>',
            ViewDefinitions = function (config, callback) {
                block = new config.form.Block({
                    selector: '.og-view.og-autocombo',
                    content: tmpl_header
                });
                config.form.on("form:load", function () {
                    menu = new og.common.util.ui.AutoCombo({
                        selector: '.og-view.og-autocombo',
                        placeholder: 'Search...',
                        source: ac_source(og.api.rest.viewdefinitions, function (viewdefs_resp) {
                            return viewdefs = (viewdefs_store = viewdefs_resp.data).pluck('name');
                        })
                    });
                    menu.block = block;
                });
                return {
                    block: block,
                    menu: menu
                };
            };

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
                    }
                });
            };
        };

        return ViewDefinitions;
    }
});
