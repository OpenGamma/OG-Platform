/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_asynchronous_h
#define __inc_og_language_util_asynchronous_h

#include "Semaphore.h"
#include "Mutex.h"
#include "Thread.h"

/// Base class for an asynchronous callback service. Operations can be passed to an instance of this
/// and will be entered onto a FIFO queue. Whenever the queue is non-empty a thread will be running
/// that will execute operations from the queue. After a period of idleness, the execution thread
/// will be released. If an operation is then added, a new thread will be created.
///
/// Users of the class may force termination of the execution thread using either the Poison or
/// RecycleThread methods, or may set an idle timeout of zero to cause the thread to terminate as
/// soon as the last operation in the queue completes.
///
/// A subclass may also attach to particular events such as a notification whenever the execution
/// thread terminates.
///
/// This is a reference counted object using the Retain and Release methods.
class CAsynchronous {
public:

	/// A callback operation that can be scheduled by a CAsynchronous instance. Instances form a
	/// singly linked list that forms the operation queue. As such, any given instance can only
	/// be passed to a single callback service which will own it and destroy it after execution
	/// (or after it is discarded in a non-executed state).
	///
	/// An operation can optionally be made 'vital' meaning it must be executed before an
	/// execution thread can terminate. Care must be taken when using this as certain resources
	/// may be locked when this execution occurs. Typically it will be required for any
	/// resources that must be freed to avoid leaks.
	class COperation : public IRunnable {
	private:
		friend class CAsynchronous;

		/// True if execution is vital, false otherwise. Non-vital operations (the default) are
		/// discarded when the scheduler is poisoned. Vital operations are always executed.
		bool m_bVital;

		/// Rescheduling count. The absolute value is the number of times the operation has been
		/// attempted. If negative, the current execution has failed and should be rescheduled.
		int m_nMustReschedule;

		/// Next operation in a singly-linked list that forms the queue.
		COperation *m_poNext;

	protected:
		COperation (bool bVital = false);
		int WasRescheduled () const;
		void MustReschedule ();

		/// Called after the operation is added to a queue before the operation can be run. Note
		/// that the state mutex for the scheduler is held, so few operations can be done
		/// safely (e.g. cannot schedule further operations).
		///
		/// @return true if the object is okay to schedule, false to cancel the scheduling
		virtual bool OnScheduled () { return true; }

	public:

		/// Destroys the operation.
		~COperation () { }
	};

private:

	/// Reference count.
	mutable CAtomicInt m_oRefCount;

	/// State mutex for the queue.
	mutable CMutex m_mutex;

	/// Head of the operation queue, NULL if the queue is empty.
	COperation *m_poHead;

	/// Tail of the operation queue, NULL if the queue is empty.
	COperation *m_poTail;

	/// Current execution thread, NULL if there is no execution thread.
	CThread *m_poRunner;

	/// True if the execution thread is waiting for an operation on the
	/// m_semQueue semaphore.
	bool m_bWaiting;

	/// Synchronisation object for the execution thread to be blocked when there
	/// are no operations in the queue. Whenever the execution thread is blocking
	/// on the semaphore, m_bWaiting will be set to true.
	CSemaphore m_semQueue;

	/// Synchronisation object between execution threads. An execution thread must
	/// hold the semaphore while it processes the queue. When threads are being
	/// recycled, this will park the new thread until the old one has processed
	/// any vital tasks and completed any callbacks expected under the old thread.
	CSemaphore m_semThread;

	/// True if the executor is poisoned, should not schedule any further operations,
	/// and release resources that are no longer required.
	bool m_bPoison;

	/// Current idle timeout of an execution thread in milliseconds.
	long m_lTimeoutInactivity;

	/// Current reschedule delay of an execution thread in milliseconds. After a callback
	/// operation has failed, requesting rescheduling, this pause will be imposed before
	/// it is retried.
	long m_lTimeoutReschedule;

	/// Current timeout before reporting an operation at INFO level to the log as being
	/// retried for this period of time. The log message will repeat at this period until
	/// the operation completes or is aborted.
	long m_lTimeoutInfoPeriod;

	/// Current timeout before abandoning an operation and proceeding to the next in the
	/// queue.
	long m_lTimeoutAbortPeriod;

	void MakeCallbacks (const CThread *poRunner);
	friend class CAsynchronousRunnerThread;
protected:
	CAsynchronous ();
	virtual ~CAsynchronous ();

	/// Notification of an execution thread terminating. This is called from the execution
	/// thread after it has run its last operation and before terminating. A subclass may
	/// use this to clean up any TLS it has allocated for example.
	virtual void OnThreadExit () { }

	/// Lock the mutex used to protect object state. This must be followed by a call to
	/// LeaveCriticalSection
	void EnterCriticalSection () const { m_mutex.Enter (); }

	/// Release the mutex used to protect object state. This must be preceded by a call to
	/// EnterCriticalSection
	void LeaveCriticalSection () const { m_mutex.Leave (); }

public:
	bool Run (COperation *poOperation);
	virtual void Poison ();
	bool RecycleThread ();

	/// Creates a new callback service
	///
	/// @return the callback service, not NULL
	static CAsynchronous *Create () { return new CAsynchronous (); }

	/// Increments the reference count.
	void Retain () const { m_oRefCount.IncrementAndGet (); }

	/// Decrements the reference count. When the count reaches zero the object is deleted. The
	/// caller must not make use of the object after it has been released.
	///
	/// @param[in] poCaller the object to release, never NULL
	static void Release (const CAsynchronous *poCaller) { if (!poCaller->m_oRefCount.DecrementAndGet ()) delete poCaller; }

	/// Poisons the object to cause termination and releases the pointer as a single step.
	///
	/// @param[in] poCaller the object to poison and release, never NULL
	static void PoisonAndRelease (CAsynchronous *poCaller) { poCaller->Poison (); Release (poCaller); }

	/// Returns the current execution thread idle timeout in milliseconds.
	///
	/// @return the idle timeout in milliseconds
	long GetTimeoutInactivity () const { return m_lTimeoutInactivity; }

	/// Sets the current execution thread idle timeout in milliseconds.
	///
	/// @param[in] lTimeoutInactivity idle timeout in milliseconds
	void SetTimeoutInactivity (long lTimeoutInactivity) { m_lTimeoutInactivity = lTimeoutInactivity; }

	/// Returns the current operation rescheduling delay in milliseconds.
	///
	/// @return the rescheduling delay in milliseconds
	long GetTimeoutReschedule () const { return m_lTimeoutReschedule; }

	/// Sets the current operation rescheduling delay in milliseconds.
	///
	/// @param[in] lTimeoutReschedule rescheduling delay in milliseconds
	void SetTimeoutReschedule (long lTimeoutReschedule) { m_lTimeoutReschedule = lTimeoutReschedule; }

	/// Returns the current period between logging operation reschedules at INFO level.
	///
	/// @return the logging period in milliseconds
	long GetTimeoutInfoPeriod () const { return m_lTimeoutInfoPeriod; }

	/// Sets the current period between logging operation reschedules at INFO level.
	///
	/// @param[in] lTimeoutInfoPeriod logging period in milliseconds
	void SetTimeoutInfoPeriod (long lTimeoutInfoPeriod) { m_lTimeoutInfoPeriod = lTimeoutInfoPeriod; }

	/// Returns the current timeout before abandoning an operation that cannot complete.
	///
	/// @return the timeout in milliseconds
	long GetTimeoutAbortPeriod () const { return m_lTimeoutAbortPeriod; }

	/// Sets the current timeout before abandoning an operation that cannot complete.
	///
	/// @param[in] lTimeoutAbortPeriod timeout in milliseconds
	void SetTimeoutAbortPeriod (long lTimeoutAbortPeriod) { m_lTimeoutAbortPeriod = lTimeoutAbortPeriod; }

};

#endif /* ifndef __inc_og_language_util_asynchronous_h */
