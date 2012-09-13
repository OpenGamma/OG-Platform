/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <windows.h>
#include <strsafe.h>
#include "control.h"
#include "autostart.h"
#include "startstop.h"
#include "status.h"
#include "Common/service.h"

#define WINDOW_CLASS		"ServiceControl"
#define WINDOW_TITLE		"OpenGamma"

#define ELEVATE_START			"start"
#define ELEVATE_STOP			"stop"
#define ELEVATE_AUTOSTART_ON	"auto"
#define ELEVATE_AUTOSTART_OFF	"manual"

#define IDC_AUTOSTART		100
#define IDC_START			101
#define IDC_STOP			102
#define IDC_STATUS			103

#define UM_SERVICESTATE		(WM_USER + 0)
#define UM_SERVICEAUTO		(WM_USER + 1)

CParamString CControl::s_oServiceName ("s", NULL, TRUE);
CParamString CControl::s_oHost ("h", "localhost");
CParamString CControl::s_oPort ("p", "8080");
CParamString CControl::s_oElevate ("elevate", NULL);

#define STATE_UNKNOWN	0	/* Any intermediate state */
#define STATE_STOPPED	1	/* Service is not running and can be started */
#define STATE_RUNNING	2	/* Service is running, and can be stopped, but the service is not responding */
#define STATE_STARTED	3	/* Service is running normally and can be stopped */

class CState {
private:
	volatile DWORD m_dwRefCount;
public:
	CRITICAL_SECTION m_cs;
	HANDLE m_hPollThread;
	HANDLE m_hPollEvent;
	volatile HWND m_hwnd;
	CService m_oService;
	volatile long m_lSequence;
	volatile int m_nServiceStatus;
	int m_nServiceCommand;
	volatile int m_nServiceState;
	CState (HWND hwnd)
	: m_oService (&CControl::s_oServiceName, &CControl::s_oHost, &CControl::s_oPort) {
		InitializeCriticalSection (&m_cs);
		m_dwRefCount = 1;
		m_hPollEvent = CreateEvent (NULL, TRUE, FALSE, NULL);
		m_hPollThread = NULL;
		m_hwnd = hwnd;
		m_nServiceStatus = 0;
		m_nServiceCommand = 0;
		m_nServiceState = STATE_UNKNOWN;
	}
	void AddRef () {
		InterlockedIncrement (&m_dwRefCount);
	}
	static void Release (CState *poState) {
		if (!InterlockedDecrement (&poState->m_dwRefCount)) {
			delete poState;
		}
	}
private:
	~CState () {
		if (m_hPollEvent) CloseHandle (m_hPollEvent);
		if (m_hPollThread) CloseHandle (m_hPollThread);
		DeleteCriticalSection (&m_cs);
	}
};

static DWORD CALLBACK _pollThread (LPVOID pState) {
	CState *poState = (CState*)pState;
	do {
		long lSequence = poState->m_lSequence;
		int nStatus = poState->m_oService.GetStatus ();
		if (nStatus != poState->m_nServiceStatus) {
			EnterCriticalSection (&poState->m_cs);
			if (poState->m_hwnd) {
				PostMessage (poState->m_hwnd, UM_SERVICEAUTO, poState->m_oService.IsAutoStart (), NULL);
				HWND hwndStatus = GetDlgItem (poState->m_hwnd, IDC_STATUS);
				if (hwndStatus) {
					char szStatus[256];
					PCSTR pszStatus = szStatus;
					int nServiceState = STATE_UNKNOWN;
					switch (nStatus) {
					case SERVICE_STATUS_BAD_CONFIG :
						pszStatus = "Configuration error";
						break;
					case SERVICE_STATUS_BAD_WINSOCK :
						StringCbPrintf (szStatus, sizeof (szStatus), "%s, error %d", "Couldn't start Winsock", GetLastError ());
						break;
					case SERVICE_STATUS_BAD_SCM :
						StringCbPrintf (szStatus, sizeof (szStatus), "%s, error %d", "Couldn't connect to SCM", GetLastError ());
						break;
					case SERVICE_STATUS_NOT_INSTALLED :
						pszStatus = "The engine service is not installed";
						break;
					case SERVICE_STATUS_CONNECTOR_ERROR :
						StringCbPrintf (szStatus, sizeof (szStatus), "%s, error %d", "Couldn't open service", GetLastError ());
						break;
					case SERVICE_STATUS_STOPPED :
						pszStatus = "The engine service is not running";
						nServiceState = STATE_STOPPED;
						break;
					case SERVICE_STATUS_BUSY :
						pszStatus = "The engine service is busy";
						break;
					case SERVICE_STATUS_QUERY_ERROR :
						StringCbPrintf (szStatus, sizeof (szStatus), "%s, error %d", "Couldn't query service status", GetLastError ());
						break;
					case SERVICE_STATUS_OK :
						pszStatus = "The engine is running and accepting connections";
						nServiceState = STATE_STARTED;
						break;
					case SERVICE_STATUS_STARTING :
						pszStatus = "Connecting to the OpenGamma engine ...";
						nServiceState = STATE_RUNNING;
						break;
					}
					if (SetWindowText (hwndStatus, pszStatus)) {
						poState->m_nServiceStatus = nStatus;
					}
					if (nServiceState != poState->m_nServiceState) {
						PostMessage (poState->m_hwnd, UM_SERVICESTATE, nServiceState, lSequence);
					}
				}
			}
			LeaveCriticalSection (&poState->m_cs);
		}
	} while (WaitForSingleObject (poState->m_hPollEvent, 1500) == WAIT_TIMEOUT);
	CState::Release (poState);
	return 0;
}

static void _stopPollingThread (CState *poState) {
	SetEvent (poState->m_hPollEvent);
	while (WaitForSingleObject (poState->m_hPollThread, 150) == WAIT_TIMEOUT) {
		MSG msg;
		if (GetMessage (&msg, NULL, 0, 0)) {
			TranslateMessage (&msg);
			DispatchMessage (&msg);
		}
	}
}

BOOL ElevateProcess (HWND hwnd, PCSTR pszCommand);

static BOOL _elevate (HWND hwnd, PCSTR pszCommand) {
	if (CControl::s_oElevate.GetString ()) {
		// Already elevated
		return FALSE;
	} else {
		if (ElevateProcess (hwnd, pszCommand)) {
			// Elevated process is launched; abandon this one
			PostMessage (hwnd, WM_CLOSE, 0, 0);
			return TRUE;
		} else {
			// Didn't elevate
			return FALSE;
		}
	}
}

static void _setAutoStart (HWND hwnd, BOOL bAutoStart, CState *poState) {
	EnableWindow (GetDlgItem (hwnd, IDC_AUTOSTART), FALSE);
	if (poState->m_oService.SetAutoStart (bAutoStart)) {
		CheckDlgButton (hwnd, IDC_AUTOSTART, bAutoStart ? BST_CHECKED : BST_UNCHECKED);
		if (bAutoStart) {
			MessageBox (hwnd, "The service will now start automatically whenever the computer restarts.", WINDOW_TITLE, MB_OK | MB_ICONINFORMATION);
		} else {
			MessageBox (hwnd, "The service must be restarted manually after a computer restart.", WINDOW_TITLE, MB_OK | MB_ICONINFORMATION);
		}
	} else {
		int nError = GetLastError ();
		if (nError == ERROR_ACCESS_DENIED) {
			if (_elevate (hwnd, bAutoStart ? ELEVATE_AUTOSTART_ON : ELEVATE_AUTOSTART_OFF)) {
				return;
			}
		}
		char sz[256];
		StringCbPrintf (sz, sizeof (sz), "Couldn't configure service, error %d", nError);
		MessageBox (hwnd, sz, WINDOW_TITLE, MB_OK | MB_ICONERROR);
	}
	EnableWindow (GetDlgItem (hwnd, IDC_AUTOSTART), TRUE);
}

static void _startService (HWND hwnd, CState *poState) {
	EnableWindow (GetDlgItem (hwnd, IDC_START), FALSE);
	SetWindowText (GetDlgItem (hwnd, IDC_STATUS), "Starting service ...");
	if (poState->m_oService.Start ()) {
		InterlockedIncrement (&poState->m_lSequence);
		poState->m_nServiceCommand = IDC_START;
		poState->m_nServiceState = STATE_UNKNOWN;
	} else {
		int nError = GetLastError ();
		if (nError == ERROR_ACCESS_DENIED) {
			if (_elevate (hwnd, ELEVATE_START)) {
				return;
			}
		}
		poState->m_nServiceCommand = 0;
		poState->m_nServiceState = STATE_UNKNOWN;
		char sz[256];
		StringCbPrintf (sz, sizeof (sz), "Couldn't start service, error %d", nError);
		MessageBox (hwnd, sz, WINDOW_TITLE, MB_OK | MB_ICONERROR);
		poState->m_nServiceStatus = SERVICE_STATUS_UNKNOWN;
	}
}

static void _stopService (HWND hwnd, CState *poState) {
	EnableWindow (GetDlgItem (hwnd, IDC_STOP), FALSE);
	SetWindowText (GetDlgItem (hwnd, IDC_STATUS), "Stopping service ...");
	if (poState->m_oService.Stop ()) {
		InterlockedIncrement (&poState->m_lSequence);
		poState->m_nServiceCommand = IDC_STOP;
		poState->m_nServiceState = STATE_UNKNOWN;
	} else {
		int nError = GetLastError ();
		if (nError == ERROR_ACCESS_DENIED) {
			if (_elevate (hwnd, ELEVATE_STOP)) {
				return;
			}
		}
		poState->m_nServiceCommand = 0;
		poState->m_nServiceState = STATE_UNKNOWN;
		char sz[256];
		StringCbPrintf (sz, sizeof (sz), "Couldn't stop service, error %d", nError);
		MessageBox (hwnd, sz, WINDOW_TITLE, MB_OK | MB_ICONERROR);
		poState->m_nServiceStatus = SERVICE_STATUS_UNKNOWN;
	}
}

static LRESULT CALLBACK _wndProc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
	switch (msg) {
	case UM_SERVICEAUTO :
		CheckDlgButton (hwnd, IDC_AUTOSTART, wp ? BST_CHECKED : BST_UNCHECKED);
		break;
	case UM_SERVICESTATE : {
		CState *poState = (CState*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		if (poState && (poState->m_lSequence == lp)) {
			switch (poState->m_nServiceState = (int)wp) {
			case STATE_UNKNOWN :
				EnableWindow (GetDlgItem (hwnd, IDC_START), FALSE);
				EnableWindow (GetDlgItem (hwnd, IDC_STOP), FALSE);
				break;
			case STATE_STOPPED :
				EnableWindow (GetDlgItem (hwnd, IDC_START), TRUE);
				EnableWindow (GetDlgItem (hwnd, IDC_STOP), FALSE);
				if (poState->m_nServiceCommand == IDC_START) {
					MessageBox (hwnd, "The service could not be started", WINDOW_TITLE, MB_OK | MB_ICONERROR);
				} else if (poState->m_nServiceCommand == IDC_STOP) {
					MessageBox (hwnd, "The service has stopped", WINDOW_TITLE, MB_OK | MB_ICONINFORMATION);
				}
				poState->m_nServiceCommand = 0;
				break;
			case STATE_RUNNING :
				EnableWindow (GetDlgItem (hwnd, IDC_START), FALSE);
				EnableWindow (GetDlgItem (hwnd, IDC_STOP), TRUE);
				break;
			case STATE_STARTED :
				EnableWindow (GetDlgItem (hwnd, IDC_START), FALSE);
				EnableWindow (GetDlgItem (hwnd, IDC_STOP), TRUE);
				if (poState->m_nServiceCommand == IDC_START) {
					MessageBox (hwnd, "The service has started", WINDOW_TITLE, MB_OK | MB_ICONINFORMATION);
				} else if (poState->m_nServiceCommand == IDC_STOP) {
					MessageBox (hwnd, "The service could not be stopped", WINDOW_TITLE, MB_OK | MB_ICONERROR);
				}
				poState->m_nServiceCommand = 0;
				break;
			}
		}
		break;
						   }
	case WM_CLOSE : {
		CState *poState = (CState*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		if (poState) {
			SetWindowLongPtr (hwnd, GWLP_USERDATA, 0);
			_stopPollingThread (poState);
			while (!TryEnterCriticalSection (&poState->m_cs)) {
				MSG msg;
				if (GetMessage (&msg, NULL, 0, 0)) {
					TranslateMessage (&msg);
					DispatchMessage (&msg);
				}
			}
			poState->m_hwnd = NULL;
			LeaveCriticalSection (&poState->m_cs);
			CState::Release (poState);
			DestroyWindow (hwnd);
		}
		break;
				   }
	case WM_COMMAND : {
		CState *poState = (CState*)GetWindowLongPtr (hwnd, GWLP_USERDATA);
		if (poState) {
			switch (wp) {
			case IDC_AUTOSTART :
				if (lp) {
					_setAutoStart (hwnd, FALSE, poState);
				} else {
					_setAutoStart (hwnd, TRUE, poState);
				}
				break;
			case IDC_START :
				_startService (hwnd, poState);
				break;
			case IDC_STOP :
				_stopService (hwnd, poState);
				break;
			}
		}
		break;
					  }
	case WM_CREATE : {
		LPCREATESTRUCT lpcs = (LPCREATESTRUCT)lp;
		RECT rc;
		GetClientRect (hwnd, &rc);
		int nHeight = CStatus::GetHeight ();
		int y = rc.bottom - nHeight;
		if (!CreateWindow (CStatus::GetClass (), "Please wait", WS_CHILD | WS_VISIBLE, rc.left, y, rc.right - rc.left, nHeight, hwnd, (HMENU)IDC_STATUS, lpcs->hInstance, NULL)) return -1;
		int nMargin = GetSystemMetrics (SM_CXICON);
		int nAutoStartHeight = CAutoStart::GetHeight ();
		int nStartStopHeight = CStartStop::GetHeight ();
		nHeight = nAutoStartHeight + (nStartStopHeight * 3);
		y = rc.top + ((y + nHeight - rc.top) >> 1);
		if (!CreateWindow (CStartStop::GetClass (), STARTSTOP_STOP, WS_CHILD | WS_VISIBLE | WS_DISABLED, rc.left + nMargin, y -= nStartStopHeight, rc.right - rc.left - (nMargin << 1), nStartStopHeight, hwnd, (HMENU)IDC_STOP, lpcs->hInstance, NULL)) return -1;
		if (!CreateWindow (CStartStop::GetClass (), STARTSTOP_START, WS_CHILD | WS_VISIBLE | WS_DISABLED, rc.left + nMargin, y -= nStartStopHeight + (nStartStopHeight >> 1), rc.right - rc.left - (nMargin << 1), nStartStopHeight, hwnd, (HMENU)IDC_START, lpcs->hInstance, NULL)) return -1;
		if (!CreateWindow (CAutoStart::GetClass (), "Start service automatically", WS_CHILD | WS_VISIBLE, rc.left + nMargin, y -= nAutoStartHeight + (nStartStopHeight >> 1), rc.right - rc.left - (nMargin << 1), nAutoStartHeight, hwnd, (HMENU)IDC_AUTOSTART, lpcs->hInstance, NULL)) return -1;
		break;
					 }
	case WM_DESTROY :
		PostQuitMessage (0L);
		break;
	default :
		return DefWindowProc (hwnd, msg, wp, lp);
	}
	return 0L;
}

BOOL CControl::Register (HINSTANCE hInstance, int nIcon) {
	WNDCLASSEX wc;
	ZeroMemory (&wc, sizeof (wc));
	wc.cbSize = sizeof (wc);
	wc.lpfnWndProc = _wndProc;
	wc.hInstance = hInstance;
	wc.hIcon = LoadIcon (hInstance, MAKEINTRESOURCE (nIcon));
	wc.hbrBackground = (HBRUSH)GetStockObject (WHITE_BRUSH);
	wc.lpszClassName = WINDOW_CLASS;
	wc.hCursor = LoadCursor (NULL, IDC_ARROW);
	if (!RegisterClassEx (&wc)) return FALSE;
	return TRUE;
}

BOOL CControl::Create (HINSTANCE hInstance, int nCmdShow) {
	if (!s_oServiceName.GetString ()) return FALSE;
	int nWidth, nHeight, nScreenWidth, nScreenHeight;
	char sz[256];
	nScreenWidth = GetSystemMetrics (SM_CXSCREEN);
	nScreenHeight = GetSystemMetrics (SM_CYSCREEN);
	nWidth = GetSystemMetrics (SM_CXICON) * 12 + GetSystemMetrics (SM_CXDLGFRAME) * 2;
	nHeight = GetSystemMetrics (SM_CYICON) * 7 + GetSystemMetrics (SM_CYDLGFRAME) * 2 + GetSystemMetrics (SM_CYCAPTION);
	StringCbPrintf (sz, sizeof (sz), "%s service", s_oServiceName.GetString ());
	HWND hwnd = CreateWindow (WINDOW_CLASS, sz, WS_DLGFRAME | WS_SYSMENU, (nScreenWidth - nWidth) >> 1, (nScreenHeight - nHeight) >> 1, nWidth, nHeight, HWND_DESKTOP, NULL, hInstance, NULL);
	if (!hwnd) return FALSE;
	ShowWindow (hwnd, nCmdShow);
	CState *poState = new CState (hwnd);
	if (!poState) return FALSE;
	SetWindowLongPtr (hwnd, GWLP_USERDATA, (LONG_PTR)poState);
	poState->AddRef ();
	poState->m_hPollThread = CreateThread (NULL, 0, _pollThread, poState, 0, NULL);
	if (!poState->m_hPollThread) {
		CState::Release (poState);
		return FALSE;
	}
	if (s_oElevate.GetString ()) {
		if (!strcmp (s_oElevate.GetString (), ELEVATE_START)) {
			PostMessage (hwnd, WM_COMMAND, IDC_START, NULL);
		} else if (!strcmp (s_oElevate.GetString (), ELEVATE_STOP)) {
			PostMessage (hwnd, WM_COMMAND, IDC_STOP, NULL);
		} else if (!strcmp (s_oElevate.GetString (), ELEVATE_AUTOSTART_ON)) {
			PostMessage (hwnd, WM_COMMAND, IDC_AUTOSTART, FALSE);
		} else if (!strcmp (s_oElevate.GetString (), ELEVATE_AUTOSTART_OFF)) {
			PostMessage (hwnd, WM_COMMAND, IDC_AUTOSTART, TRUE);
		} else {
			// Invalid elevation command; bomb out
			return FALSE;
		}
	}
	return TRUE;
}

void CControl::DispatchMessages () {
	MSG msg;
	while (GetMessage (&msg, NULL, 0, 0)) {
		TranslateMessage (&msg);
		DispatchMessage (&msg);
	}
}