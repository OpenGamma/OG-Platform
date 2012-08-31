/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "feedbackwindow.h"

#define WINDOW_CLASS	"OGFeedback"

#define UM_DESTROY		(WM_USER + 1)
#define UM_SETSTATUS	(WM_USER + 2)

LRESULT CALLBACK CFeedbackWindow::WndProc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
	CFeedbackWindow *poWnd = (CFeedbackWindow*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
	switch (msg) {
	case UM_DESTROY :
		DestroyWindow (hwnd);
		break;
	case UM_SETSTATUS :
		if (poWnd) poWnd->OnSetStatusText ((PCSTR)lp);
		break;
	case WM_CLOSE :
		if (poWnd) poWnd->OnClose ();
		break;
	case WM_DESTROY :
		if (poWnd) {
			SetWindowLongPtr (hwnd, GWLP_USERDATA, 0);
			poWnd->OnDestroy ();
			Release (poWnd);
		}
		break;
	case WM_PAINT : {
		HDC hdc;
		PAINTSTRUCT ps;
		hdc = BeginPaint (hwnd, &ps);
		if (hdc) {
			if (poWnd) poWnd->OnPaint (hdc, &ps);
			EndPaint (hwnd, &ps);
		}
		break;
					}
	default :
		return DefWindowProc (hwnd, msg, wp, lp);
	}
	return 0L;
}

CFeedbackWindow::CFeedbackWindow (HWND hwndParent, HINSTANCE hInstance, PCSTR pszTitle) {
	m_dwRefCount = 2; // Caller and the one embedded in the window
	int nWidth, nHeight, nScreenWidth, nScreenHeight;
	nScreenWidth = GetSystemMetrics (SM_CXSCREEN);
	nScreenHeight = GetSystemMetrics (SM_CYSCREEN);
	nWidth = GetSystemMetrics (SM_CXICON) * 12 + GetSystemMetrics (SM_CXDLGFRAME) * 2;
	nHeight = GetSystemMetrics (SM_CYICON) * 3 + GetSystemMetrics (SM_CYDLGFRAME) * 2 + GetSystemMetrics (SM_CYCAPTION);
	m_hwnd = CreateWindow (WINDOW_CLASS, pszTitle, WS_DLGFRAME | WS_SYSMENU, (nScreenWidth - nWidth) >> 1, (nScreenHeight - nHeight) >> 1, nWidth, nHeight, hwndParent, NULL, hInstance, NULL);
	SetWindowLongPtr (m_hwnd, GWLP_USERDATA, (LONG_PTR)this);
	WNDCLASSEX wc;
	ZeroMemory (&wc, sizeof (wc));
	wc.cbSize = sizeof (wc);
	if (GetClassInfoEx (hInstance, WINDOW_CLASS, &wc)) {
		m_hicon = wc.hIcon;
	} else {
		m_hicon = NULL;
	}
	m_pszStatus = _strdup ("Please wait");
}

CFeedbackWindow::~CFeedbackWindow () {
	delete m_pszStatus;
}

BOOL CFeedbackWindow::Register (HINSTANCE hInstance, int nIcon) {
	WNDCLASSEX wc;
	ZeroMemory (&wc, sizeof (wc));
	wc.cbSize = sizeof (wc);
	wc.lpfnWndProc = WndProc;
	wc.hInstance = hInstance;
	wc.hIcon = LoadIcon (hInstance, MAKEINTRESOURCE (nIcon));
	wc.hbrBackground = (HBRUSH)GetStockObject (WHITE_BRUSH);
	wc.lpszClassName = WINDOW_CLASS;
	wc.hCursor = LoadCursor (hInstance, IDC_WAIT);
	if (!RegisterClassEx (&wc)) return FALSE;
	return TRUE;
}

void CFeedbackWindow::Release (CFeedbackWindow *po) {
	if (po) {
		if (!InterlockedDecrement (&po->m_dwRefCount)) {
			delete po;
		}
	}
}

void CFeedbackWindow::AddRef () {
	InterlockedIncrement (&m_dwRefCount);
}

int CFeedbackWindow::DispatchMessages () {
	MSG msg;
	while (GetMessage (&msg, NULL, 0, 0)) {
		TranslateMessage (&msg);
		DispatchMessage (&msg);
	}
	return (int)msg.wParam;
}

void CFeedbackWindow::OnClose () {
	DestroyWindow (m_hwnd);
}

void CFeedbackWindow::OnDestroy () {
	// No-op
}

void CFeedbackWindow::Destroy () {
	SendMessage (m_hwnd, UM_DESTROY, 0, 0);
}

static BOOL _dontMeasure (char c) {
	return (c == '.') || (c == ',') || (c == ' ');
}

static int _wrapText (HDC hdc, PCSTR pcText, int cchText, int nX, int nY, int nBaseline, int nWidth) {
	SIZE s;
	int cchMeasure = cchText;
	while ((cchMeasure > 0) && _dontMeasure (pcText[cchMeasure - 1])) {
		cchMeasure--;
	}
	GetTextExtentPoint (hdc, pcText, cchMeasure, &s);
	if (s.cx > nWidth) {
		int cchFirst = cchText >> 1;
		while (pcText[cchFirst] != ' ') {
			if (++cchFirst >= cchText) {
				cchFirst = cchText >> 1;
				while (pcText[cchFirst] != ' ') {
					if (--cchFirst <= 0) goto drawText;
				}
				break;
			}
		}
		int nHeight;
		if (nBaseline < 0) {
			nHeight = _wrapText (hdc, pcText, cchFirst, nX, nY, -1, nWidth);
			nHeight += _wrapText (hdc, pcText + cchFirst + 1, cchText - (cchFirst + 1), nX, nY + nHeight, -1, nWidth);
		} else if (nBaseline > 0) {
			nHeight = _wrapText (hdc, pcText + cchFirst + 1, cchText - (cchFirst + 1), nX, nY, 1, nWidth);
			nHeight += _wrapText (hdc, pcText, cchFirst, nX, nY - nHeight, 1, nWidth);
		} else {
		  nHeight = _wrapText (hdc, pcText, cchFirst, nX, nY, 1, nWidth)
		          + _wrapText (hdc, pcText + cchFirst + 1, cchText - (cchFirst + 1), nX, nY, -1, nWidth);
		}
		return nHeight;
	}
drawText:
	if (nBaseline < 0) {
		nY += s.cy >> 3;
	} else if (nBaseline > 0) {
		nY -= s.cy + (s.cy >> 3);
	} else {
		nY -= s.cy >> 1;
	}
	TextOut (hdc, nX - (s.cx >> 1), nY, pcText, cchText);
	return s.cy + (s.cy >> 2);
}

void CFeedbackWindow::OnPaint (HDC hdc, PPAINTSTRUCT pps) {
	RECT rc;
	GetClientRect (m_hwnd, &rc);
	int nIcoWidth = GetSystemMetrics (SM_CXICON);
	int nIcoHeight = GetSystemMetrics (SM_CYICON);
	DrawIcon (hdc, rc.left + (nIcoWidth >> 1), (rc.bottom - nIcoHeight) >> 1, m_hicon);
	if (m_pszStatus) {
		HFONT hfont = CreateFont ((GetSystemMetrics (SM_CYMENUSIZE) * 8) / 10, 0, 0, 0, FW_DONTCARE, FALSE, FALSE, FALSE, ANSI_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE, TEXT ("MS Shell Dlg"));
		if (hfont) {
			SelectObject (hdc, hfont);
			_wrapText (hdc, m_pszStatus, (int)strlen (m_pszStatus), (rc.right + rc.left) >> 1, (rc.top + rc.bottom) >> 1, 0, (rc.right - rc.left) - (nIcoWidth << 2));
			DeleteObject (hfont);
		}
	}
}

void CFeedbackWindow::OnSetStatusText (PCSTR pszText) {
	delete m_pszStatus;
	if (!pszText) {
		m_pszStatus = NULL;
		return;
	}
	m_pszStatus = _strdup (pszText);
	InvalidateRect (m_hwnd, NULL, TRUE);
}

void CFeedbackWindow::SetStatusText (PCSTR pszText) {
	SendMessage (m_hwnd, UM_SETSTATUS, 0, (LPARAM)pszText);
}

void CFeedbackWindow::BringToTop () {
	// Put window to the very top, then shift it back so it ends up at the front of all non-topmost windows.
	SetWindowPos (m_hwnd, HWND_TOPMOST, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE);
	SetWindowPos (m_hwnd, HWND_NOTOPMOST, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE);
}
