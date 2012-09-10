/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "autostart.h"

#define WINDOW_CLASS	"AutoStart"

struct _wndData {
	BOOL bCapturing;
	BOOL bChecked;
};

static void _paint (HWND hwnd, HDC hdc) {
	RECT rc;
	GetClientRect (hwnd, &rc);
	char sz[MAX_PATH];
	int n = GetWindowText (hwnd, sz, sizeof (sz) / sizeof (char));
	int cx = GetSystemMetrics (SM_CXICON);
	int cy = GetSystemMetrics (SM_CYICON);
	HFONT hfont = CreateFont (GetSystemMetrics (SM_CYMENUSIZE), 0, 0, 0, FW_DONTCARE, FALSE, FALSE, FALSE, ANSI_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE, TEXT ("MS Shell Dlg"));
	if (hfont) {
		SelectObject (hdc, hfont);
		SIZE size;
		GetTextExtentPoint (hdc, sz, n, &size);
		SetBkMode (hdc, TRANSPARENT);
		TextOut (hdc, rc.left + (cx << 1), (rc.top + rc.bottom - size.cy) >> 1, sz, n);
		SelectObject (hdc, GetStockObject (SYSTEM_FONT));
		DeleteObject (hfont);
	}
	int x = rc.left + cx + (cx >> 1);
	int y = (rc.top + rc.bottom) >> 1;
	Rectangle (hdc, x - (cx >> 1), y - (cy >> 2), x, y + (cy >> 2));
	struct _wndData *pData = (struct _wndData*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
	if (pData->bChecked) {
		HPEN hp = CreatePen (PS_SOLID, 2, RGB (0, 0, 0));
		if (hp) {
			SelectObject (hdc, hp);
			MoveToEx (hdc, x - (cx >> 1) + 2, y - (cy >> 2) + 2, NULL);
			LineTo (hdc, x - 3, y + (cy >> 2) - 3);
			MoveToEx (hdc, x - (cx >> 1) + 2, y + (cy >> 2) - 3, NULL);
			LineTo (hdc, x - 3, y - (cy >> 2) + 2);
			SelectObject (hdc, GetStockObject (BLACK_PEN));
			DeleteObject (hp);
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
	case BM_GETCHECK : {
		struct _wndData *pData = (struct _wndData*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		return pData->bChecked;
					   }
	case BM_SETCHECK : {
		struct _wndData *pData = (struct _wndData*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		BOOL bChecked = (wp == BST_CHECKED);
		if (pData->bChecked != bChecked) {
			pData->bChecked = bChecked;
			InvalidateRect (hwnd, NULL, FALSE);
		}
		break;
					   }
	case WM_CREATE : {
		struct _wndData *pData = new struct _wndData;
		if (!pData) return -1;
		pData->bCapturing = FALSE;
		pData->bChecked = FALSE;
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
	case WM_LBUTTONDOWN : {
		struct _wndData *pData = (struct _wndData*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		PostMessage (GetParent (hwnd), WM_COMMAND, GetDlgCtrlID (hwnd), pData->bChecked);
		break;
						  }
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
		} else {
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

BOOL CAutoStart::Register (HINSTANCE hInstance) {
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

int CAutoStart::GetHeight () {
	return GetSystemMetrics (SM_CYICON);
}

PCSTR CAutoStart::GetClass () {
	return WINDOW_CLASS;
}