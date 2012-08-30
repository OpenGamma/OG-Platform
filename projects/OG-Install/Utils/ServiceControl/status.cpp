/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "status.h"

#define WINDOW_CLASS	"Status"

static void _paint (HWND hwnd, HDC hdc) {
	RECT rc;
	char sz[MAX_PATH];
	int n = GetWindowText (hwnd, sz, sizeof (sz) / sizeof (char));
	GetClientRect (hwnd, &rc);
	HFONT hfont = CreateFont ((GetSystemMetrics (SM_CYMENUSIZE) * 8) / 10, 0, 0, 0, FW_DONTCARE, FALSE, FALSE, FALSE, ANSI_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE, TEXT ("MS Shell Dlg"));
	if (hfont) {
		SelectObject (hdc, hfont);
		SIZE size;
		GetTextExtentPoint (hdc, sz, n, &size);
		SetBkMode (hdc, TRANSPARENT);
		TextOut (hdc, (rc.right + rc.left - size.cx) >> 1, (rc.top + rc.bottom - size.cy) >> 1, sz, n);
		SelectObject (hdc, GetStockObject (SYSTEM_FONT));
		DeleteObject (hfont);
	}
}

static LRESULT CALLBACK _wndProc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
	switch (msg) {
	case WM_PAINT : {
		PAINTSTRUCT ps;
		HDC hdc = BeginPaint (hwnd, &ps);
		_paint (hwnd, hdc);
		EndPaint (hwnd, &ps);
		break;
					}
	case WM_SETTEXT : {
		LRESULT lResult = DefWindowProc (hwnd, msg, wp, lp);
		if (lResult) {
			InvalidateRect (hwnd, NULL, TRUE);
		}
		return lResult;
					  }
	default :
		return DefWindowProc (hwnd, msg, wp, lp);
	}
	return 0L;
}

BOOL CStatus::Register (HINSTANCE hInstance) {
	WNDCLASSEX wc;
	ZeroMemory (&wc, sizeof (wc));
	wc.cbSize = sizeof (wc);
	wc.lpfnWndProc = _wndProc;
	wc.hInstance = hInstance;
	wc.hbrBackground = (HBRUSH)GetStockObject (LTGRAY_BRUSH);
	wc.lpszClassName = WINDOW_CLASS;
	wc.hCursor = LoadCursor (NULL, IDC_ARROW);
	if (!RegisterClassEx (&wc)) return FALSE;
	return TRUE;
}

int CStatus::GetHeight () {
	int nHeight = GetSystemMetrics (SM_CYMENU);
	return nHeight + (nHeight >> 1);
}

PCSTR CStatus::GetClass () {
	return WINDOW_CLASS;
}
