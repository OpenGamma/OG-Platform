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
            focused: 'dropmenu:focused',
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
            if (config && config.hasOwnProperty('cntr')) {
                if (config.hasOwnProperty('tmpl')) {
                    menu.$dom.cntr = config.cntr.html($((Handlebars.compile(config.tmpl || ''))(config.data || {})));
                    menu.$dom.toggle = $('.og-menu-toggle', menu.$dom.cntr);
                    menu.$dom.menu = $('.og-menu', menu.$dom.cntr);
                } else {
                    menu.$dom.cntr = config.cntr;
                    menu.$dom.toggle = $('.og-menu-toggle', menu.$dom.cntr);
                    menu.$dom.toggle.on('click', menu.toggle_handler.bind(menu));
                    menu.$dom.menu = $('.og-menu', menu.$dom.cntr);
                }
            }
            menu.on(events.open, menu.open.bind(menu))
                .on(events.close, menu.close.bind(menu))
                .on(events.focus, menu.focus.bind(menu));
            return menu;
        };
        DropMenu.prototype.constructor = DropMenu;
        DropMenu.prototype.fire = og.common.events.fire;
        DropMenu.prototype.on = og.common.events.on;
        DropMenu.prototype.off = og.common.events.off;
        DropMenu.prototype.focus = function () {
            return this.opened = true, this.state = 'focused', this.fire(events.focused), this;
        };
        DropMenu.prototype.open = function () {
            var menu = this, innerwidth = $(document.body).innerWidth(), totalwidth = 0, menuwidth = 0;
            if (menu.$dom.menu) {
                menu.$dom.menu.parents().each(function (idx, elem) {
                    totalwidth += $(elem).position().left;
                });
                menuwidth = menu.$dom.menu.width() + totalwidth;
                if (menuwidth > innerwidth) menu.$dom.menu.css('left', (innerwidth-menuwidth) - 20 + 'px');
                else menu.$dom.menu.css('left', '');
                setTimeout(function () {
                    if (menu.init_blurkill) return;
                    menu.init_blurkill = true;
                    menu.$dom.menu.blurkill(menu.close.bind(menu));
                });
                menu.$dom.menu.show();
                og.views.common.layout.main.allowOverflow('north');
                menu.state = 'open';
                menu.opened = true;
                menu.$dom.toggle.addClass('og-active');
                menu.fire(events.opened);
            }
            return menu;
        };
        DropMenu.prototype.close = function () {
            var menu = this;
            if (menu.$dom.menu) menu.$dom.menu.hide();
            if (menu.$dom.toggle) menu.$dom.toggle.removeClass('og-active');
            menu.state = 'closed';
            menu.opened = menu.init_blurkill = false;
            menu.fire(events.closed);
            og.views.common.layout.main.resetOverflow('north');
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