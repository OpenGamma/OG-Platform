/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <windows.h>
#include "startstop.h"

#define WINDOW_CLASS	"StartStop"

struct _wndData {
	BOOL bCapturing;
};

static void _paint (HWND hwnd, HDC hdc) {
	RECT rc;
	char sz[MAX_PATH];
	int n = GetWindowText (hwnd, sz, sizeof (sz) / sizeof (char));
	int cx = GetSystemMetrics (SM_CXICON);
	int cy = GetSystemMetrics (SM_CYICON);
	GetClientRect (hwnd, &rc);
	HFONT hfont = CreateFont (GetSystemMetrics (SM_CYMENUSIZE), 0, 0, 0, FW_DONTCARE, FALSE, FALSE, FALSE, ANSI_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE, TEXT ("MS Shell Dlg"));
	if (hfont) {
		SelectObject (hdc, hfont);
		SIZE size;
		GetTextExtentPoint (hdc, sz, n, &size);
		SetBkMode (hdc, TRANSPARENT);
		SetTextColor (hdc, IsWindowEnabled (hwnd) ? RGB (0, 0, 0) : RGB (128, 128, 128));
		TextOut (hdc, rc.left + (cx << 1), (rc.top + rc.bottom - size.cy) >> 1, sz, n);
		SelectObject (hdc, GetStockObject (SYSTEM_FONT));
		DeleteObject (hfont);
	}
	int x = rc.left + cx + (cx >> 1);
	int y = (rc.top + rc.bottom) >> 1;
	SelectObject (hdc, GetStockObject (NULL_PEN));
	if (!strcmp (sz, STARTSTOP_START)) {
		HBRUSH hbr = CreateSolidBrush (IsWindowEnabled (hwnd) ? RGB (0, 192, 0) : RGB (192, 192, 192));
		if (hbr) {
			POINT apt[3];
			SelectObject (hdc, hbr);
			apt[0].x = x;
			apt[0].y = y;
			apt[1].x = x - (cx >> 1);
			apt[1].y = y + (cy >> 2);
			apt[2].x = x - (cx >> 1);
			apt[2].y = y - (cy >> 2);
			Polygon (hdc, apt, 3);
			SelectObject (hdc, GetStockObject (WHITE_BRUSH));
			DeleteObject (hbr);
		}
	} else if (!strcmp (sz, STARTSTOP_STOP)) {
		HBRUSH hbr = CreateSolidBrush (IsWindowEnabled (hwnd) ? RGB (192, 0, 0) : RGB (192, 192, 192));
		if (hbr) {
			SelectObject (hdc, hbr);
			Rectangle (hdc, x - (cx >> 1), y - (cy >> 2), x, y + (cy >> 2));
			SelectObject (hdc, GetStockObject (WHITE_BRUSH));
			DeleteObject (hbr);
		}
	}
}

static void _erasebkgnd (HWND hwnd, HDC hdc) {
	RECT rc;
	HPEN hp = NULL;
	HBRUSH hbr = NULL;
	do {
		GetClientRect (hwnd, &rc);
		hp = CreatePen (PS_SOLID, 1, RGB (255, 128, 128));
		if (!hp) break;
		SelectObject (hdc, hp);
		hbr = CreateSolidBrush (RGB (255, 255, 128));
		if (!hbr) break;
		SelectObject (hdc, hbr);
		Rectangle (hdc, rc.left, rc.top, rc.right, rc.bottom);
	} while (FALSE);
	if (hp) {
		SelectObject (hdc, GetStockObject (BLACK_PEN));
		DeleteObject (hp);
	}
	if (hbr) {
		SelectObject (hdc, GetStockObject (WHITE_BRUSH));
		DeleteObject (hbr);
	}
}

static LRESULT CALLBACK _wndProc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
	switch (msg) {
	case WM_CREATE : {
		struct _wndData *pData = new struct _wndData;
		if (!pData) return -1;
		pData->bCapturing = FALSE;
		SetWindowLongPtr (hwnd, GWLP_USERDATA, (LONG_PTR)pData);
		break;
					 }
	case WM_DESTROY : {
		struct _wndData *pData = (struct _wndData*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		delete pData;
		SetWindowLongPtr (hwnd, GWLP_USERDATA, 0);
		break;
					  }
	case WM_ENABLE :
		if (!wp) {
			struct _wndData *pData = (struct _wndData*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
			if (pData->bCapturing) {
				SetCursor (LoadCursor (NULL, IDC_ARROW));
				pData->bCapturing = FALSE;
				ReleaseCapture ();
			}
		}
		InvalidateRect (hwnd, NULL, TRUE);
		break;
	case WM_ERASEBKGND : {
		struct _wndData *pData = (struct _wndData*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		if (pData->bCapturing) {
			_erasebkgnd (hwnd, (HDC)wp);
		} else {
			return DefWindowProc (hwnd, msg, wp, lp);
		}
		break;
						 }
	case WM_LBUTTONDOWN :
		PostMessage (GetParent (hwnd), WM_COMMAND, GetDlgCtrlID (hwnd), NULL);
		break;
	case WM_MOUSEMOVE : {
		struct _wndData *pData = (struct _wndData*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		if (pData->bCapturing) {
			WORD wX = LOWORD (lp);
			WORD wY = HIWORD (lp);
			RECT rc;
			GetClientRect (hwnd, &rc);
			if ((wX < rc.left) || (wX > rc.right)
			 || (wY < rc.top) || (wY > rc.bottom)) {
				SetCursor (LoadCursor (NULL, IDC_ARROW));
				pData->bCapturing = FALSE;
				ReleaseCapture ();
				InvalidateRect (hwnd, NULL, TRUE);
			}
		} else if (IsWindowEnabled (hwnd)) {
			pData->bCapturing = TRUE;
			SetCapture (hwnd);
			SetCursor (LoadCursor (NULL, IDC_HAND));
			InvalidateRect (hwnd, NULL, TRUE);
		}
		break;
						}
	case WM_PAINT : {
		PAINTSTRUCT ps;
		HDC hdc = BeginPaint (hwnd, &ps);
		_paint (hwnd, hdc);
		EndPaint (hwnd, &ps);
		break;
					}
	default :
		return DefWindowProc (hwnd, msg, wp, lp);
	}
	return 0L;
}

BOOL CStartStop::Register (HINSTANCE hInstance) {
	WNDCLASSEX wc;
	ZeroMemory (&wc, sizeof (wc));
	wc.cbSize = sizeof (wc);
	wc.lpfnWndProc = _wndProc;
	wc.hInstance = hInstance;
	wc.hbrBackground = (HBRUSH)GetStockObject (WHITE_BRUSH);
	wc.lpszClassName = WINDOW_CLASS;
	wc.hCursor = LoadCursor (NULL, IDC_ARROW);
	if (!RegisterClassEx (&wc)) return FALSE;
	return TRUE;
}

int CStartStop::GetHeight () {
	return GetSystemMetrics (SM_CYICON);
}

PCSTR CStartStop::GetClass () {
	return WINDOW_CLASS;
}
