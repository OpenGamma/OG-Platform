/*
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Fudge.h"
#include "Logging.h"

LOGGING (com.opengamma.language.util.Fudge);

static size_t _hash (const void *pData, size_t cbData) {
	size_t *psData = (size_t*)pData;
	size_t hc = 1;
	int i;
	for (i = cbData / sizeof (size_t); i > 0; i--) {
		hc += (hc << 4) + *(psData++);
	}
	size_t cbOfs = cbData % sizeof (size_t);
	if (cbOfs > 0) {
		char *pcData = (char*)pData + (cbData - cbOfs);
		while (cbOfs-- > 0) {
			hc += (hc << 4) + *(pcData++);
		}
	}
	return hc;
}

static size_t _hash (FudgeString string) {
	return _hash (FudgeString_getData (string), FudgeString_getSize (string));
}

static size_t _hash (FudgeField *pField) {
	size_t hc;
	switch (pField->type) {
		case FUDGE_TYPE_INDICATOR :
			hc = 0;
			break;
		case FUDGE_TYPE_BOOLEAN :
			hc = pField->data.boolean ? 1 : 0;
			break;
		case FUDGE_TYPE_BYTE :
			hc = (size_t)pField->data.byte;
			break;
		case FUDGE_TYPE_SHORT :
			hc = (size_t)pField->data.i16;
			break;
		case FUDGE_TYPE_INT :
			hc = (size_t)pField->data.i32;
			break;
		case FUDGE_TYPE_LONG :
			hc = (size_t)pField->data.i64;
			break;
		case FUDGE_TYPE_FLOAT :
			hc = _hash (&pField->data.f32, sizeof (fudge_f32));
			break;
		case FUDGE_TYPE_DOUBLE :
			hc = _hash (&pField->data.f64, sizeof (fudge_f64));
			break;
		default :
			hc = _hash (pField->data.bytes, pField->numbytes);
			break;
		case FUDGE_TYPE_STRING :
			hc = _hash (pField->data.string);
			break;
		case FUDGE_TYPE_FUDGE_MSG :
			hc = FudgeMsg_hash (pField->data.message);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_4 :
			hc = _hash (pField->data.bytes, 4);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_8 :
			hc = _hash (pField->data.bytes, 8);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_16 :
			hc = _hash (pField->data.bytes, 16);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_20 :
			hc = _hash (pField->data.bytes, 20);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_32 :
			hc = _hash (pField->data.bytes, 32);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_64 :
			hc = _hash (pField->data.bytes, 64);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_128 :
			hc = _hash (pField->data.bytes, 128);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_256 :
			hc = _hash (pField->data.bytes, 256);
			break;
		case FUDGE_TYPE_BYTE_ARRAY_512 :
			hc = _hash (pField->data.bytes, 512);
			break;
		case FUDGE_TYPE_DATE :
			hc = _hash (&pField->data.datetime.date, sizeof (FudgeDate));
			break;
		case FUDGE_TYPE_TIME :
			hc = _hash (&pField->data.datetime.time, sizeof (FudgeTime));
			break;
		case FUDGE_TYPE_DATETIME :
			hc = _hash (&pField->data.datetime, sizeof (FudgeDateTime));
			break;
	}
	hc += (hc << 4) + pField->ordinal;
	if (pField->name) {
		hc += _hash (pField->name);
	}
	return hc;
}

/// Hashes a Fudge message.
///
/// @param[in] msg the message to hash
/// @return the hash code
size_t FudgeMsg_hash (const FudgeMsg msg) {
	unsigned long ulFields = FudgeMsg_numFields (msg);
	if (ulFields == 0) {
		return 0;
	}
	FudgeField *pField = new FudgeField[ulFields];
	if (!pField) {
		LOGFATAL (TEXT ("Out of memory"));
		return 0;
	}
	size_t hc = 1;
	if (FudgeMsg_getFields (pField, (fudge_i32)ulFields, msg) > 0) {
		unsigned long ul;
		for (ul = 0; ul < ulFields; ul++) {
			hc += (hc << 4) + _hash (pField + ul);
		}
	}
	delete pField;
	return hc;
}

static int _compare (FudgeField *pA, FudgeField *pB) {
	int c;
	if (pA->type < pB->type) return -1;
	if (pA->type > pB->type) return 1;
	if (pA->ordinal < pB->ordinal) return -1;
	if (pA->ordinal > pB->ordinal) return 1;
	if (pA->name) {
		if (pB->name) {
			c = FudgeString_compare (pA->name, pB->name);
			if (c) return c;
		} else {
			return -1;
		}
	} else {
		if (pB->name) return 1;
	}
	switch (pA->type) {
		case FUDGE_TYPE_INDICATOR :
			return 0;
		case FUDGE_TYPE_BOOLEAN :
			return pA->data.boolean - pB->data.boolean;
		case FUDGE_TYPE_BYTE :
			return pA->data.byte - pB->data.byte;
		case FUDGE_TYPE_SHORT :
			return pA->data.i16 - pB->data.i16;
		case FUDGE_TYPE_INT :
			return pA->data.i32 - pB->data.i32;
		case FUDGE_TYPE_LONG :
			if (pA->data.i64 < pB->data.i64) {
				return -1;
			} else if (pA->data.i64 > pB->data.i64) {
				return 1;
			} else {
				return 0;
			}
		case FUDGE_TYPE_FLOAT :
			if (pA->data.f32 < pB->data.f32) {
				return -1;
			} else if (pA->data.f32 > pB->data.f32) {
				return 1;
			} else {
				return 0;
			}
		case FUDGE_TYPE_DOUBLE :
			if (pA->data.f64 < pB->data.f64) {
				return -1;
			} else if (pA->data.f64 > pB->data.f64) {
				return 1;
			} else {
				return 0;
			}
		case FUDGE_TYPE_DATE :
			return memcmp (&pA->data.datetime.date, &pB->data.datetime.date, sizeof (FudgeDate));
		case FUDGE_TYPE_TIME :
			return memcmp (&pA->data.datetime.time, &pB->data.datetime.time, sizeof (FudgeTime));
		case FUDGE_TYPE_DATETIME :
			return memcmp (&pA->data.datetime, &pB->data.datetime, sizeof (FudgeDateTime));
		case FUDGE_TYPE_STRING :
			return FudgeString_compare (pA->data.string, pB->data.string);
		case FUDGE_TYPE_FUDGE_MSG :
			return FudgeMsg_compare (pA->data.message, pB->data.message);
		default :
			c = pA->numbytes - pB->numbytes;
			if (!c) {
				c = memcmp (pA->data.bytes, pB->data.bytes, pA->numbytes);
			}
			return c;
		case FUDGE_TYPE_BYTE_ARRAY_4 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 4);
		case FUDGE_TYPE_BYTE_ARRAY_8 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 8);
		case FUDGE_TYPE_BYTE_ARRAY_16 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 16);
		case FUDGE_TYPE_BYTE_ARRAY_20 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 20);
		case FUDGE_TYPE_BYTE_ARRAY_32 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 32);
		case FUDGE_TYPE_BYTE_ARRAY_64 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 64);
		case FUDGE_TYPE_BYTE_ARRAY_128 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 128);
		case FUDGE_TYPE_BYTE_ARRAY_256 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 256);
		case FUDGE_TYPE_BYTE_ARRAY_512 :
			return memcmp (&pA->data.bytes, &pB->data.bytes, 512);
	}
}

/// Compares two Fudge messages
///
/// @param[in] a the first message
/// @param[in] b the second message
/// @return negative if the first message is less, positive is greater, 0 if equal
int FudgeMsg_compare (const FudgeMsg a, const FudgeMsg b) {
	unsigned long ulFields = FudgeMsg_numFields (a);
	int c = ulFields - FudgeMsg_numFields (b);
	if (c) {
		return c;
	}
	FudgeField *pA = NULL, *pB = NULL;
	do {
		pA = new FudgeField[ulFields];
		pB = new FudgeField[ulFields];
		if (!pA || !pB) {
			LOGFATAL (TEXT ("Out of memory"));
			break;
		}
		if ((FudgeMsg_getFields (pA, (fudge_i32)ulFields, a) <= 0) || (FudgeMsg_getFields (pB, (fudge_i32)ulFields, b) <= 0)) {
			break;
		}
		unsigned long ul;
		for (ul = 0; ul < ulFields; ul++) {
			c = _compare (pA + ul, pB + ul);
			if (c) {
				break;
			}
		}
	} while (false);
	delete pA;
	delete pB;
	return c;
}
