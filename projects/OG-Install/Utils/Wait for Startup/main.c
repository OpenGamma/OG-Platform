/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#define _WINSOCKAPI_
#include <windows.h>
#include <tchar.h>
#include <strsafe.h>
#include <ws2tcpip.h>
#include "resource.h"

#define STR_EXPECTED_s_AFTER_s_FLAG	TEXT ("Expected %s after '%s' flag")
#define WINDOW_CLASS TEXT ("OGFeedback")
#define WAITING_MESSAGE TEXT ("Waiting for %s to start")

static const TCHAR *g_pszTitle = TEXT ("OpenGamma");
static const TCHAR *g_pszService = NULL;
static const TCHAR *g_pszHost = TEXT ("localhost");
static const TCHAR *g_pszPort = TEXT ("8080");
static HWND g_hwnd = NULL;

/// The warning message to be posted to the user. If NULL means we are still waiting for something to happen.
static const TCHAR *g_pszStatus = NULL;

static BOOL _fault (const TCHAR *pszMessage) {
	MessageBox (HWND_DESKTOP, pszMessage, g_pszTitle, MB_OK | MB_ICONSTOP);
	return FALSE;
}

static BOOL _fault_n (const TCHAR *pszFormat, int n) {
	TCHAR sz[256];
	StringCbPrintf (sz, sizeof (sz), pszFormat, n);
	return _fault (sz);
}

static BOOL _fault_sz_sz (const TCHAR *pszFormat, const TCHAR *psz1, const TCHAR *psz2) {
	TCHAR sz[256];
	StringCbPrintf (sz, sizeof (sz), pszFormat, psz1, psz2);
	return _fault (sz);
}

static BOOL _fault_n_sz (const TCHAR *pszFormat, int n, const TCHAR *psz) {
	TCHAR sz[256];
	StringCbPrintf (sz, sizeof (sz), pszFormat, n, psz);
	return _fault (sz);
}

static BOOL _getargs2 (int argc, const TCHAR **argv) {
	int i;
	for (i = 1; i < argc; i++) {
		if ((argv[i][0] == '-') || (argv[i][0] == '/')) {
			switch (argv[i][1]) {
			case 'h' :
				if (++i >= argc) return _fault_sz_sz (STR_EXPECTED_s_AFTER_s_FLAG, TEXT ("host name"), argv[i - 1] + 1);
				g_pszHost = _tcsdup(argv[i]);
				break;
			case 'p' :
				if (++i >= argc) return _fault_sz_sz (STR_EXPECTED_s_AFTER_s_FLAG, TEXT ("port number"), argv[i - 1] + 1);
				g_pszPort = _tcsdup (argv[i]);
				break;
			case 's' :
				if (++i >= argc) return _fault_sz_sz (STR_EXPECTED_s_AFTER_s_FLAG, TEXT ("service name"), argv[i - 1] + 1);
				g_pszService = _tcsdup(argv[i]);
				break;
			case 't' :
				if (++i >= argc) return _fault_sz_sz (STR_EXPECTED_s_AFTER_s_FLAG, TEXT ("window title"), argv[i - 1] + 1);
				g_pszTitle = _tcsdup(argv[i]);
				break;
			default :
				return _fault_n_sz (TEXT ("Parameter %d unrecognised switch %s"), i, argv[i] + 1);
			}
		} else {
			return _fault_n_sz (TEXT ("Parameter %d unrecognised - %s"), i, argv[i]);
		}
	}
	if (!g_pszService) return _fault (TEXT ("Service not specified; use -s <service name>"));
	return TRUE;
}

static BOOL _getargs () {
	int argc;
	TCHAR **argv;
	BOOL bResult;
#ifdef _UNICODE
	argv = CommandLineToArgvW (GetCommandLineW (), &argc);
#else /* ifdef _UNICODE */
#error "TODO: ansi command line parse"
#endif /* ifdef _UNICODE */
	if (!argv) return _fault (TEXT ("Couldn't parse command line"));
	bResult = _getargs2 (argc, argv);
	LocalFree (argv);
	return bResult;
}

static void _dispatchMessages () {
	MSG msg;
	while (GetMessage (&msg, NULL, 0, 0)) {
		TranslateMessage (&msg);
		DispatchMessage (&msg);
	}
}

static void _paintWindow (HWND hwnd) {
	PAINTSTRUCT ps;
	HDC hdc;
	TCHAR sz[256];
	const TCHAR *psz;
	SIZE s;
	int l;
	RECT rc;
	HFONT hf = NULL;
	do {
		hdc = BeginPaint (hwnd, &ps);
		if (!hdc) break;
		hf = CreateFont ((GetSystemMetrics (SM_CYMENUSIZE) * 8) / 10, 0, 0, 0, FW_DONTCARE, FALSE, FALSE, FALSE, ANSI_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY, DEFAULT_PITCH | FF_DONTCARE, TEXT ("MS Shell Dlg"));
		if (!hf) break;
		SelectObject (hdc, hf);
		if (g_pszStatus) {
			psz = g_pszStatus;
		} else {
			StringCbPrintf (sz, sizeof (sz), WAITING_MESSAGE, g_pszTitle);
			psz = sz;
		}
		l = _tcslen (psz);
		GetTextExtentPoint (hdc, psz, l, &s);
		GetClientRect (hwnd, &rc);
		TextOut (hdc, rc.left + ((rc.right - rc.left - s.cx) >> 1), rc.top + ((rc.bottom - rc.top - s.cy) >> 1), psz, l);
	} while (FALSE);
	if (hdc) EndPaint (hwnd, &ps);
	if (hf) DeleteObject (hf);
}

LRESULT CALLBACK _wndProc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
	switch (msg) {
	case WM_CLOSE :
		if (!g_pszStatus) {
			TCHAR sz[256];
			StringCbPrintf (sz, sizeof (sz), TEXT ("%s has not finished starting yet. Do you want to wait for it to finish?"), g_pszTitle);
			if (MessageBox (hwnd, sz, g_pszTitle, MB_YESNO | MB_ICONQUESTION) != IDNO) {
				break;
			}
		}
		PostQuitMessage (0);
		break;
	case WM_PAINT :
		_paintWindow (hwnd);
		break;
	case WM_USER :
		if (lp) {
			g_pszStatus = (const TCHAR *)lp;
			InvalidateRect (hwnd, NULL, TRUE);
			MessageBox (hwnd, g_pszStatus, g_pszTitle, MB_OK | MB_ICONWARNING);
		}
		PostQuitMessage (0);
		break;
	default :
		return DefWindowProc (hwnd, msg, wp, lp);
	}
	return 0L;
}

static BOOL _registerClass (HINSTANCE hInstance) {
	WNDCLASSEX wc;
	ZeroMemory (&wc, sizeof (wc));
	wc.cbSize = sizeof (wc);
	wc.lpfnWndProc = _wndProc;
	wc.hInstance = hInstance;
	wc.hIcon = LoadIcon (hInstance, MAKEINTRESOURCE (IDI_OPENGAMMA));
	wc.hbrBackground = (HBRUSH)GetStockObject (WHITE_BRUSH);
	wc.lpszClassName = WINDOW_CLASS;
	wc.hCursor = LoadCursor (hInstance, IDC_WAIT);
	if (!RegisterClassEx (&wc)) {
		return _fault_n (TEXT ("Couldn't register windowing class, error %d"), GetLastError ());
	}
	return TRUE;
}

static BOOL _createWindow (HINSTANCE hInstance, int nCmdShow) {
	int nWidth, nHeight, nScreenWidth, nScreenHeight;
	nScreenWidth = GetSystemMetrics (SM_CXSCREEN);
	nScreenHeight = GetSystemMetrics (SM_CYSCREEN);
	nWidth = GetSystemMetrics (SM_CXICON) * 12 + GetSystemMetrics (SM_CXDLGFRAME) * 2;
	nHeight = GetSystemMetrics (SM_CYICON) * 3 + GetSystemMetrics (SM_CYDLGFRAME) * 2 + GetSystemMetrics (SM_CYCAPTION);
	g_hwnd = CreateWindow (WINDOW_CLASS, g_pszTitle, WS_DLGFRAME | WS_SYSMENU, (nScreenWidth - nWidth) >> 1, (nScreenHeight - nHeight) >> 1, nWidth, nHeight, HWND_DESKTOP, NULL, hInstance, NULL);
	if (!g_hwnd) return _fault_n (TEXT ("Couldn't create feedback window, error %d"), GetLastError ());
	ShowWindow (g_hwnd, nCmdShow);
	UpdateWindow (g_hwnd);
	BringWindowToTop (g_hwnd);
	return TRUE;
}

static BOOL _socketConnect () {
	ADDRINFOW *aiService = NULL, aiHint;
	BOOL bResult = FALSE;
	SOCKET sock = INVALID_SOCKET;
	int i;
	do {
		ZeroMemory (&aiHint, sizeof (aiHint));
		aiHint.ai_family = AF_INET;
		aiHint.ai_socktype = SOCK_STREAM;
		aiHint.ai_protocol = IPPROTO_TCP;
		if (GetAddrInfo (g_pszHost, g_pszPort, &aiHint, &aiService) != 0) break;
		sock = socket (aiService->ai_family, aiService->ai_socktype, aiService->ai_protocol);
		if (sock == INVALID_SOCKET) break;
		i = 3000;
		setsockopt (sock, SOL_SOCKET, SO_RCVTIMEO, (char*)&i, sizeof (i));
		setsockopt (sock, SOL_SOCKET, SO_SNDTIMEO, (char*)&i, sizeof (i));
		if (connect (sock, aiService->ai_addr, aiService->ai_addrlen) != 0) break;
		bResult = TRUE;
	} while (FALSE);
	if (aiService) FreeAddrInfo (aiService);
	if (sock) closesocket (sock);
	return bResult;
}

static int _startWinsock () {
	WSADATA wsa;
	return WSAStartup (0x0002, &wsa);
}

static DWORD WINAPI _waitForStartup (LPVOID reserved) {
	static TCHAR szStatus[256] = TEXT ("");
	SC_HANDLE hSCM = NULL;
	SC_HANDLE hService = NULL;
	int nWinsock = -1;
	SERVICE_STATUS ss;
	do {
		nWinsock = _startWinsock ();
		if (nWinsock) {
			StringCbPrintf (szStatus, sizeof (szStatus), TEXT ("Internal error loading library, error %d"), nWinsock);
			break;
		}
		hSCM = OpenSCManager (NULL, NULL, GENERIC_READ);
		if (!hSCM) {
			StringCbPrintf (szStatus, sizeof (szStatus), TEXT ("Couldn't open service manager, error %d"), GetLastError ());
			break;
		}
		hService = OpenService (hSCM, g_pszService, SERVICE_INTERROGATE);
		if (!hService) {
			DWORD dwError = GetLastError ();
			switch (dwError) {
			case ERROR_SERVICE_DOES_NOT_EXIST :
				StringCbPrintf (szStatus, sizeof (szStatus), TEXT ("The %s service was not installed"), g_pszService);
				break;
			default :
				StringCbPrintf (szStatus, sizeof (szStatus), TEXT ("Couldn't connect to %s, error %d"), g_pszService, dwError);
				break;
			}
			break;
		}
		do {
			if (!ControlService (hService, SERVICE_CONTROL_INTERROGATE, &ss)) {
				DWORD dwError = GetLastError ();
				switch (dwError) {
				case ERROR_SERVICE_NEVER_STARTED :
				case ERROR_SERVICE_NOT_ACTIVE :
					StringCbPrintf (szStatus, sizeof (szStatus), TEXT ("A problem occurred during the installation of the %s service and it was not started"), g_pszService);
					break;
				default :
					StringCbPrintf (szStatus, sizeof (szStatus), TEXT ("Couldn't query %s, error %d"), g_pszService, GetLastError ());
					break;
				}
				break;
			}
			if (ss.dwCurrentState != SERVICE_RUNNING) {
				StringCbPrintf (szStatus, sizeof (szStatus), TEXT ("The %s service could not be started"), g_pszService);
				break;
			}
			if (_socketConnect ()) {
				break;
			}
			Sleep (1000);
		} while (!*szStatus);
	} while (FALSE);
	if (nWinsock == 0) WSACleanup ();
	if (hSCM) CloseServiceHandle (hSCM);
	if (hService) CloseServiceHandle (hService);
	BringWindowToTop (g_hwnd);
	SetWindowPos (g_hwnd, HWND_TOPMOST, 0, 0, 0, 0, SWP_NOMOVE | SWP_NOSIZE);
	PostMessage (g_hwnd, WM_USER, 0, (LPARAM)(szStatus[0] ? szStatus : NULL));
	return 0;
}

static BOOL _startThread () {
	HANDLE hThread = CreateThread (NULL, 0, _waitForStartup, NULL, 0, NULL);
	if (hThread == INVALID_HANDLE_VALUE) return _fault_n (TEXT ("Couldn't create wait thread, error %d"), GetLastError ());
	CloseHandle (hThread);
	return TRUE;
}

int WINAPI WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance, char * pszCmdLine, int nCmdShow) {
	int nResult = EXIT_FAILURE;
	do {
		if (!_getargs ()) break;
		if (!_registerClass (hInstance)) break;
		if (!_createWindow (hInstance, nCmdShow)) break;
		if (!_startThread ()) break;
		_dispatchMessages ();
		nResult = EXIT_SUCCESS;
	} while (FALSE);
	return nResult;
}
