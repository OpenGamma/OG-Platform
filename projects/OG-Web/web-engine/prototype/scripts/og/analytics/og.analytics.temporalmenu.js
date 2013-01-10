/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.TemporalMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () {
        return function (config) {
            if (!config) return og.dev.warn('og.analytics.TemporalMenu: Missing param [config] to constructor.');

            if (!('form' in config) || !config.form)
                return og.dev.warn('og.analytics.TemporalMenu: Missing param key [config.form] to constructor.');

            // Private
            var default_conf = {
                    form: config.form,
                    selector: '.og-temporal',
                    tmpl: 'og.analytics.form_temporal_tash'
                },
                menu, initialized = false;

            var init = function (conf) {
                if (($dom = menu.$dom) && $dom.menu) $dom.menu.on('click', 'input', menu_handler);
                 menu.fire('initialized', [initialized = true]);
            };

            var menu_handler = function (event) {
                var $elem = $(event.srcElement || event.target), entry;
            };

            return menu = new og.analytics.DropMenu(default_conf, init);
        };
    }
});