/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.DropMenu',
    dependencies: [],
    obj: function () {
        var events = {
            focus: 'dropmenu:focus',
            focused:'dropmenu:focused',
            open: 'dropmenu:open',
            opened: 'dropmenu:opened',
            close: 'dropmenu:close',
            closed: 'dropmenu:closed'
        };
        var DropMenu = function (config) {
            var menu = this;
            menu.state = 'closed';
            menu.opened = false;
            menu.init_blurkill = false;
            menu.events = events;
            menu.$dom = {};
            if (config && config.hasOwnProperty('tmpl') && config.hasOwnProperty('data')) {
                menu.$dom.cntr = config.$cntr.html($((Handlebars.compile(config.tmpl||''))(config.data||{})));
                menu.$dom.toggle = $('.og-menu-toggle', menu.$dom.cntr);
                menu.$dom.menu = $('.og-menu', menu.$dom.cntr);
            }
            menu.addListener(events.open, menu.open.bind(menu))
                .addListener(events.close, menu.close.bind(menu))
                .addListener(events.focus, menu.focus.bind(menu));
            return menu;
        };
        $.extend(true, DropMenu.prototype, EventEmitter.prototype);
        DropMenu.prototype.constructor = DropMenu;
        DropMenu.prototype.focus = function () {
            return this.opened = true, this.state = 'focused', this.emitEvent(events.focused), this;
        };
        DropMenu.prototype.open = function () {
            var menu = this;
            if (menu.$dom.menu) {
                setTimeout(function () {
                    if (menu.init_blurkill) return;
                    menu.init_blurkill = true;
                    menu.$dom.menu.blurkill(menu.close.bind(menu));
                });
                menu.$dom.menu.show();
                menu.state = 'open';
                menu.opened = true;
                menu.$dom.toggle.addClass('og-active');
                menu.emitEvent(events.opened);
            }
            return menu;
        };
        DropMenu.prototype.close = function () {
            var menu = this;
            if (menu.$dom.menu) menu.$dom.menu.hide();
            if (menu.$dom.toggle) menu.$dom.toggle.removeClass('og-active');
            menu.state = 'closed';
            menu.opened = menu.init_blurkill = false;
            menu.emitEvent(events.closed);
            return menu;
        };
        DropMenu.prototype.menu_handler = function () {
            throw new Error ('Inheriting class needs to extend this method');
        };
        DropMenu.prototype.toggle_handler = function () {
            return this.opened ? this.close() : (this.open(), this.focus());
        };
        DropMenu.prototype.stop = function (event) {
            event.stopPropagation();
            event.preventDefault();
        };
        DropMenu.prototype.status = function () {
            return this.state;
        };
        DropMenu.prototype.is_opened = function () {
            return this.opened;
        };
        return DropMenu;
    }
});