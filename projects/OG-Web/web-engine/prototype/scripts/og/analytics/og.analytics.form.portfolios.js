/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.Portfolios',
    dependencies: ['og.common.util.ui.AutoCombo'],
    obj: function () {
        var module = this, menu, block, portfolios, portfolio_store,
            Portfolios = function (config, callback) {
                block = new config.form.Block({
                    selector: '.og-portfolios.og-autocombo',
                    processor: function (data) {
                        var port = portfolio_store.filter(function (entry) {
                            return entry.split('|')[2] === menu.$input.val();
                        });
                        data.portfolio = port[0].split('|')[0];
                    }
                });
                config.form.on("form:load", function () {
                    menu = new og.common.util.ui.AutoCombo({
                        selector:'.og-portfolios.og-autocombo',
                        placeholder: 'Search Portfolios...',
                        source: ac_source(og.api.rest.portfolios, store_porfolios)
                    });
                    if (config.val) {
                        menu.$input.val(config.val);
                        og.api.rest.portfolios.get().pipe(store_porfolios);
                    }
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

        var store_porfolios = function (resp) {
            return portfolios = (portfolio_store = resp.data.data).map(function (entry) {
                return entry.split('|')[2];
            });
        };

        return Portfolios;
    }
});
