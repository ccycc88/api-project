package com.api.project.util;

public class CRC64 {

	class crc_hash_t {
		public int h1;
		public int h2;

		public crc_hash_t() {
			this.h1 = 0;
			this.h2 = 0;
		}

		public crc_hash_t(int theH1, int theH2) {
			this.h1 = theH1;
			this.h2 = theH2;
		}
	};

	int crc_HashLimit = 64;

	/*
	 * Poly: 0x00600340.00F0D50A
	 */

	int HINIT1 = 0xFAC432B1;
	int HINIT2 = 0x0CD5E44A;

	int POLY1 = 0x00600340;
	int POLY2 = 0x00F0D50B;

	crc_hash_t[] CrcXor;
	crc_hash_t[] Poly;

	public CRC64() {
		CrcXor = new crc_hash_t[256];
		for (int i = 0; i < 256; i++) {
			CrcXor[i] = new crc_hash_t();
		}
		Poly = new crc_hash_t[64 + 1];
		for (int i = 0; i < 64 + 1; i++) {
			Poly[i] = new crc_hash_t();
		}
	}

	void crc_init() {
		int i;

		/*
		 * Polynomials to use for various crc sizes. Start with the 64 bit polynomial
		 * and shift it right to generate the polynomials for fewer bits. Note that the
		 * polynomial for N bits has no bit set above N-8. This allows us to do a simple
		 * table-driven CRC.
		 */

		Poly[64].h1 = POLY1;
		Poly[64].h2 = POLY2;
		for (i = 63; i >= 16; --i) {
			Poly[i].h1 = Poly[i + 1].h1 >>> 1;
			Poly[i].h2 = (Poly[i + 1].h2 >>> 1) | ((Poly[i + 1].h1 & 1) << 31) | 1;
		}

		for (i = 0; i < 256; ++i) {
			int j;
			int v = i;
			crc_hash_t hv = new crc_hash_t(0, 0);
			for (j = 0; j < 8; ++j, v = v << 1) {
				hv.h1 <<= 1;
				if ((hv.h2 & 0x80000000L) != 0)
					hv.h1 |= 1;
				hv.h2 = (hv.h2 << 1);
				if ((v & 0x80) != 0) {
					hv.h1 ^= Poly[crc_HashLimit].h1;
					hv.h2 ^= Poly[crc_HashLimit].h2;
				}
			}
			CrcXor[i] = hv;
		}
	}

	/*
	 * testhash() - do the CRC. The complexity is simply due to the programmable
	 * nature of the number of bits. We extract the top 8 bits to use as a table
	 * lookup to obtain the polynomial XOR 8 bits at a time rather then 1 bit at a
	 * time.
	 */

	crc_hash_t crc_calculate(String pStr, int len, crc_hash_t hv) {
		crc_hash_t phv = hv;
		int e = len;
		int p = 0;

		if (crc_HashLimit <= 32) {
			int s = crc_HashLimit - 8;
			int m = (int) -1 >>> (32 - crc_HashLimit);

			hv.h1 = 0;
			hv.h2 &= m;

			while (p < e) {
				int i = (hv.h2 >>> s) & 255;
				/* printf("i = %d %08lx\n", i, CrcXor[i].h2); */
				hv.h2 = ((hv.h2 << 8) & m) ^ pStr.charAt(p) ^ CrcXor[i].h2;
				++p;
			}
			System.out.print("1:phv->h2:");
			System.out.println(phv.h2);
		} else if (crc_HashLimit < 32 + 8) {
			int s2 = 32 + 8 - crc_HashLimit; /* bits in byte from h2 */
			int m = (int) -1 >>> (64 - crc_HashLimit);

			hv.h1 &= m;
			while (p < e) {
				int i = ((hv.h1 << s2) | (hv.h2 >>> (32 - s2))) & 255;
				hv.h1 = (((hv.h1 << 8) ^ (int) (hv.h2 >>> 24)) & m) ^ CrcXor[i].h1;
				hv.h2 = (hv.h2 << 8) ^ pStr.charAt(p) ^ CrcXor[i].h2;
				++p;
			}
			System.out.print("2:phv->h2:");
			System.out.println(phv.h2);
		} else {
			int s = crc_HashLimit - 40;
			int m = (int) -1 >>> (64 - crc_HashLimit);
			hv.h1 &= m;
			while (p < e) {
				int h1 = hv.h1;
				int h2 = hv.h2;
				int i = (h1 >>> s) & 255;
				hv.h1 = ((h1 << 8) & m) ^ (int) ((h2 >>> 24) & 255) ^ CrcXor[i].h1;
				hv.h2 = (h2 << 8) ^ pStr.charAt(p) ^ CrcXor[i].h2;
				++p;
			}
		}
		return phv;
	}

	// 取得CRC码
	// 参数：source 原始字符串，dest 转换成CRC码之后的字符串
	// 返回值：0 成功，否则变失败
	public long getcrc64id(String source) {
		crc_hash_t result_hash;
		int h1 = 0;// low
		int h2 = 0;// high
		int high = 0;
		int low = 0;
		long a1 = 0x100000000L;
		long result;

		crc_init();
		crc_hash_t hv = new crc_hash_t(HINIT1, HINIT2);
		result_hash = crc_calculate(source, source.length(), hv);
		h1 = result_hash.h1;
		h2 = result_hash.h2;
		if (h1 > 0x7FFFFFFF) {
			low = -1 * (0xFFFFFFFF - h1) - 1;
		} else {
			low = h1;
		}
		if (h2 > 0x7FFFFFFF) {
			high = -1 * (0xFFFFFFFF - h2) - 1;
		} else {
			high = h2;
		}

		result = high * a1 + low;

		return result;
	}

	/*
	 * ECMA: 0x42F0E1EBA9EA3693 / 0xC96C5795D7870F42 / 0xA17870F5D4F51B49
	 */
	/*
	 * private static final long POLY64 = 0x42F0E1EBA9EA3693L; private static final
	 * long[] LOOKUPTABLE;
	 * 
	 * static { LOOKUPTABLE = new long[0x100]; for (int i = 0; i < 0x100; i++) {
	 * long crc = i; for (int j = 0; j < 8; j++) { if ((crc & 1) == 1) { crc = (crc
	 * >>> 1) ^ POLY64; } else { crc = (crc >>> 1); } } LOOKUPTABLE[i] = crc; } }
	 * 
	 * /** The checksum of the data
	 * 
	 * @param data The data to checksum
	 * 
	 * @return The checksum of the data
	 */
	/*
	 * @Override public long digest(final byte[] data) { long checksum = 0;
	 * 
	 * for (int i = 0; i < data.length; i++) { final int lookupidx = ((int) checksum
	 * ^ data[i]) & 0xff; checksum = (checksum >>> 8) ^ LOOKUPTABLE[lookupidx]; }
	 * 
	 * return checksum; }
	 * 
	 * long checksum1 = 0; public String update(final byte[] data) { long checksum =
	 * digest(data); checksum1 = checksum; return Long.toString(checksum); }
	 * 
	 * public long getValue1(){ return checksum1; }
	 * 
	 */ static public void main(String[] argv) throws Exception {
		CRC64 crc = new CRC64();
		System.out.println(crc.getcrc64id("OdsaasdsasK"));

	}
}
