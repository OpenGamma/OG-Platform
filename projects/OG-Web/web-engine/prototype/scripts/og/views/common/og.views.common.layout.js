/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.layout',
    dependencies: [],
    obj: function () {
        return {
            'default': function () {return {
                main: $('.ui-layout-container').layout({
                    defaults: {enableCursorHotkey: false},
                    north:    {spacing_open: 0, size: 43, paneClass: 'ui-layout-masthead'},
                    south:    {spacing_open: 0, size: 36, paneClass: 'ui-layout-footer'},
                    east:     {spacing_closed: 0, initClosed: true}, // Not used
                    west:     {size: '33%', paneClass: 'ui-layout-search'},
                    center:   {paneClass: 'ui-layout-details'}
                }),
                inner: $('.ui-layout-center').layout({
                    defaults: {enableCursorHotkey: false},
                    north: { // used for deleted view message
                        paneSelector: '.ui-layout-inner-north', paneClass: 'ui-layout-inner-north',
                        size: 50, initClosed: true, spacing_closed: 0, spacing_open: 0
                    },
                    south: { // versions / sync etc
                        paneSelector: '.ui-layout-inner-south', paneClass: 'ui-layout-inner-south',
                        size: '50%', initClosed: true, spacing_closed: 0,
                        onopen_start: function () {$('.ui-layout-inner-south').empty();}
                    },
                    center: {
                        paneSelector: '.ui-layout-inner-center', contentSelector: '.ui-layout-content',
                        paneClass: 'ui-layout-inner-center'
                    }
                })
            };},
            'analytics': function () {return {
                main: $('.ui-layout-container').layout({
                    defaults: {enableCursorHotkey: false},
                    north: {spacing_open: 0, paneClass: 'ui-layout-masthead', size: 43},
                    south: {spacing_open: 0, paneClass: 'ui-layout-footer', size: 36},
                    center: {paneClass: 'ui-layout-analytics', contentSelector: '.ui-layout-content'}
                })
            };},
            'analytics2': function () {return {
                main: $('.ui-layout-container').layout({
                    defaults: {enableCursorHotkey: false},
                    north: {spacing_open: 0, paneClass: 'ui-layout-masthead', size: 43},
                    south: {spacing_open: 0, paneClass: 'ui-layout-footer', size: 36},
                    east: {initClosed: true, spacing_closed: 0},
                    west: {size: '33%', initClosed: true, spacing_closed: 0, spacing_open: 0},
                    center: {paneClass: 'ui-layout-analytics', contentSelector: '.ui-layout-content'}
                })
            };}
        };
    }
});