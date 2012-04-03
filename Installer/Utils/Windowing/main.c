/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#define WINDOWS_LEAN_AND_MEAN
#include <Windows.h>
#include <TlHelp32.h>
#include <tchar.h>
#include <strsafe.h>

struct _enumInfo {
	DWORD dwProcess;
	int nFound;
	BOOL bMinimize;
};

static BOOL CALLBACK _enumWindows (HWND hwnd, LPARAM lpei) {
	if (IsWindowVisible (hwnd)) {
		struct _enumInfo *pei = (struct _enumInfo*)lpei;
		DWORD dwProcessId;
		if (GetWindowThreadProcessId (hwnd, &dwProcessId)) {
			if (dwProcessId == pei->dwProcess) {
				pei->nFound++;
				if (pei->bMinimize) {
					if (!IsIconic (hwnd)) {
						ShowWindow (hwnd, SW_MINIMIZE);
					}
				} else {
					if (IsIconic (hwnd)) {
						ShowWindow (hwnd, SW_RESTORE);
					}
				}
			}
		}
	}
	return TRUE;
}

static BOOL _promoteWindows (DWORD dwProcess, DWORD dwPseudoChild) {
	struct _enumInfo ei;
	HANDLE hSnapshot;
	ei.bMinimize = FALSE;
	ei.dwProcess = dwProcess;
	ei.nFound = 0;
	hSnapshot = CreateToolhelp32Snapshot (TH32CS_SNAPPROCESS, 0);
	if (hSnapshot != INVALID_HANDLE_VALUE) {
		PROCESSENTRY32 pe32;
		ZeroMemory (&pe32, sizeof (PROCESSENTRY32));
		pe32.dwSize = sizeof (PROCESSENTRY32);
		if (Process32First (hSnapshot, &pe32)) {
			do {
				if ((pe32.th32ParentProcessID == ei.dwProcess) && (pe32.th32ProcessID > 0)) {
					if (_promoteWindows (pe32.th32ProcessID, 0)) {
						ei.bMinimize = TRUE;
					}
				}
			} while (Process32Next (hSnapshot, &pe32));
			if (dwPseudoChild > 0) {
				if (_promoteWindows (dwPseudoChild, 0)) {
					ei.bMinimize = TRUE;
				}
			}
		}
		CloseHandle (hSnapshot);
	}
	EnumWindows (_enumWindows, (LPARAM)&ei);
	return ei.bMinimize || (ei.nFound > 0);
}

static DWORD _protectedMsiExec () {
	DWORD dwResult = 0;
	SC_HANDLE hSCM = NULL;
	SC_HANDLE hService = NULL;
	SERVICE_STATUS_PROCESS ssp;
	DWORD dw;
	do {
		hSCM = OpenSCManager (NULL, NULL, GENERIC_READ);
		if (!hSCM) break;
		hService = OpenService (hSCM, TEXT ("msiserver"), SERVICE_QUERY_STATUS);
		if (!hService) break;
		if (!QueryServiceStatusEx (hService, SC_STATUS_PROCESS_INFO, (LPBYTE)&ssp, sizeof (ssp), &dw)) break;
		if (ssp.dwCurrentState != SERVICE_RUNNING) break;
		dwResult = ssp.dwProcessId;
	} while (FALSE);
	if (hSCM) CloseServiceHandle (hSCM);
	if (hService) CloseServiceHandle (hService);
	return dwResult;
}

int _tmain (int argc, const TCHAR **argv) {
	if (argc == 2) {
		DWORD dwParent = _tcstol (argv[1], NULL, 10);
		HANDLE hParent = OpenProcess (PROCESS_QUERY_INFORMATION | SYNCHRONIZE, FALSE, dwParent);
		if (hParent != INVALID_HANDLE_VALUE) {
			do {
				_promoteWindows (dwParent, _protectedMsiExec ());
			} while (WaitForSingleObject (hParent, 1500) == WAIT_TIMEOUT);
			CloseHandle (hParent);
		}
	} else {
		HANDLE hSnapshot;
		hSnapshot = CreateToolhelp32Snapshot (TH32CS_SNAPPROCESS, 0);
		if (hSnapshot != INVALID_HANDLE_VALUE) {
			PROCESSENTRY32 pe32;
			DWORD pid = GetProcessId (GetCurrentProcess ());
			ZeroMemory (&pe32, sizeof (PROCESSENTRY32));
			pe32.dwSize = sizeof (PROCESSENTRY32);
			if (Process32First (hSnapshot, &pe32)) {
				do {
					if (pe32.th32ProcessID == pid) {
						TCHAR sz[32];
						STARTUPINFO si;
						PROCESS_INFORMATION pi;
						StringCbPrintf (sz, sizeof (sz), TEXT ("windowing.exe %d"), pe32.th32ParentProcessID);
						ZeroMemory (&si, sizeof (si));
						si.cb = sizeof (si);
						if (CreateProcess (argv[0], sz, NULL, NULL, FALSE, CREATE_NO_WINDOW, NULL, NULL, &si, &pi)) {
							CloseHandle (pi.hProcess);
							CloseHandle (pi.hThread);
						}
						break;
					}
				} while (Process32Next (hSnapshot, &pe32));
			}
			CloseHandle (hSnapshot);
		}
	}
	return 0;
}
