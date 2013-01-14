/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.TemporalMenu',
    dependencies: ['og.analytics.DropMenu'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var TemporalMenu = function (config) {
            if (!config) return og.dev.warn('og.analytics.TemporalMenu: Missing param [config] to constructor.');

            if (!('form' in config) || !config.form)
                return og.dev.warn('og.analytics.TemporalMenu: Missing param key [config.form] to constructor.');

            // Private
            var menu = this, initialized = false, form = config.form;

            form.Block.call(menu, { selector: '.og-temporal', module: 'og.analytics.form_temporal_tash' });

            var init = function (conf) {
                 menu.fire('initialized', [initialized = true]);
            };

            var menu_handler = function (event) {
                var $elem = $(event.srcElement || event.target), entry;
            };

        };
        TemporalMenu.prototype = new Block;
        return TemporalMenu;
    }
});