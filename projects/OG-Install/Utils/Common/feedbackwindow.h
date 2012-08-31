/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_common_feedbackwindow_h
#define __inc_common_feedbackwindow_h

class CFeedbackWindow {
private:
	volatile DWORD m_dwRefCount;
	HWND m_hwnd;
	HICON m_hicon;
	PCTSTR m_pszStatus;
	static LRESULT CALLBACK WndProc (HWND hwnd, UINT msg, WPARAM wp, LPARAM lp);
	LRESULT WndProc (UINT msg, WPARAM wp, LPARAM lp);
	void OnPaint (HDC hdc, PPAINTSTRUCT pps);
	void OnSetStatusText (PCSTR pszText);
protected:
	~CFeedbackWindow ();
	virtual void OnClose ();
	virtual void OnDestroy ();
public:
	CFeedbackWindow (HWND hwndParent, HINSTANCE hInstance, PCSTR pszTitle);
	static BOOL Register (HINSTANCE hInstance, int nIcon);
	void Show (int nCmdShow) { ShowWindow (m_hwnd, nCmdShow); }
	void BringToTop ();
	void Destroy ();
	int Alert (PCSTR pszText, PCSTR pszCaption, UINT uType) { return MessageBox (m_hwnd, pszText, pszCaption, uType); }
	static void Release (CFeedbackWindow *po);
	void AddRef ();
	void SetStatusText (PCSTR pszText);
	static int DispatchMessages ();
};

#endif /* ifndef __inc_common_feedbackwindow_h */
