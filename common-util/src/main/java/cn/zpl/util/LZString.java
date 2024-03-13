/*
 * LZString4Java By Rufus Huang 
 * https://github.com/rufushuang/lz-string4java
 * MIT License
 * 
 * Port from original JavaScript version by pieroxy 
 * https://github.com/pieroxy/lz-string
 */

package cn.zpl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LZString {

	private static char[] keyStrBase64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
	private static char[] keyStrUriSafe = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$".toCharArray();
	private static Map<char[], Map<Character, Integer>> baseReverseDic = new HashMap<char[], Map<Character, Integer>>();

	private static char getBaseValue(char[] alphabet, Character character) {
		Map<Character, Integer> map = baseReverseDic.get(alphabet);
		if (map == null) {
			map = new HashMap<Character, Integer>();
			baseReverseDic.put(alphabet, map);
			for (int i = 0; i < alphabet.length; i++) {
				map.put(alphabet[i], i);
			}
		}
		return (char) map.get(character).intValue();
	}
	
	public static String compressToBase64(String input) {
		if (input == null)
			return "";
		String res = LZString._compress(input, 6, new CompressFunctionWrapper() {
			@Override
			public char doFunc(int a) {
				return keyStrBase64[a];
			}
		});
		switch (res.length() % 4) { // To produce valid Base64
		default: // When could this happen ?
		case 0:
			return res;
		case 1:
			return res + "===";
		case 2:
			return res + "==";
		case 3:
			return res + "=";
		}
	}
	
	public static String decompressFromBase64(final String inputStr) {
		if (inputStr == null)
			return "";
		if (inputStr.equals(""))
			return null;
		return LZString._decompress(inputStr.length(), 32, new DecompressFunctionWrapper() {
			@Override
			public char doFunc(int index) {
				return getBaseValue(keyStrBase64, inputStr.charAt(index));
			}
		});
	}	

	public static String compressToUTF16(String input) {
		if (input == null)
			return "";
		return LZString._compress(input, 15, new CompressFunctionWrapper() {
			@Override
			public char doFunc(int a) {
				return fc(a + 32);
			}
		}) + " ";
	}

	public static String decompressFromUTF16(final String compressedStr) {
		if (compressedStr == null)
			return "";
		if (compressedStr.isEmpty())
			return null;
		return LZString._decompress(compressedStr.length(), 16384, new DecompressFunctionWrapper() {
			@Override
			public char doFunc(int index) {
				return (char) (compressedStr.charAt(index) - 32);
			}
		});
	}
	
	//TODO: java has no Uint8Array type, what can we do?
	
	public static String compressToEncodedURIComponent(String input) {
		if (input == null)
			return "";
		return LZString._compress(input, 6, new CompressFunctionWrapper() {
			@Override
			public char doFunc(int a) {
				return keyStrUriSafe[a];
			}
		});
	}

	public static String decompressFromEncodedURIComponent(String inputStr) {
	    if (inputStr == null) return "";
	    if (inputStr.isEmpty()) return null;
	    final String urlEncodedInputStr = inputStr.replace(' ', '+');
		return LZString._decompress(urlEncodedInputStr.length(), 32, new DecompressFunctionWrapper() {
			@Override
			public char doFunc(int index) {
				return getBaseValue(keyStrUriSafe, urlEncodedInputStr.charAt(index));
			}
		});
	}

	private static abstract class CompressFunctionWrapper {
		public abstract char doFunc(int i);
	}

	public static String compress(String uncompressed) {
		return LZString._compress(uncompressed, 16, new CompressFunctionWrapper() {
			@Override
			public char doFunc(int a) {
				return fc(a);
			}
		});
	}
	private static String _compress(String uncompressedStr, int bitsPerChar, CompressFunctionWrapper getCharFromInt) {
	    if (uncompressedStr == null) return "";
		int i, value;
		Map<String, Integer> context_dictionary = new HashMap<String, Integer>();
		Set<String> context_dictionaryToCreate = new HashSet<String>();
		String context_c = "";
		String context_wc = "";
		String context_w = "";
		int context_enlargeIn = 2; // Compensate for the first entry which should not count
		int context_dictSize = 3;
		int context_numBits = 2;
		StringBuilder context_data = new StringBuilder(uncompressedStr.length() / 3);
		int context_data_val = 0;
		int context_data_position = 0;
		int ii;
		
		for (ii = 0; ii < uncompressedStr.length(); ii += 1) {
			context_c = String.valueOf(uncompressedStr.charAt(ii));
			if (!context_dictionary.containsKey(context_c)) {
				context_dictionary.put(context_c, context_dictSize++);
				context_dictionaryToCreate.add(context_c);
			}

			context_wc = context_w + context_c;
			if (context_dictionary.containsKey(context_wc)) {
				context_w = context_wc;
			} else {
				if (context_dictionaryToCreate.contains(context_w)) {
					if (context_w.charAt(0) < 256) {
						for (i = 0; i < context_numBits; i++) {
							context_data_val = (context_data_val << 1);
							if (context_data_position == bitsPerChar - 1) {
								context_data_position = 0;
								context_data.append(getCharFromInt.doFunc(context_data_val));
								context_data_val = 0;
							} else {
								context_data_position++;
							}
						}
						value = context_w.charAt(0);
						for (i = 0; i < 8; i++) {
							context_data_val = (context_data_val << 1) | (value & 1);
							if (context_data_position == bitsPerChar - 1) {
								context_data_position = 0;
								context_data.append(getCharFromInt.doFunc(context_data_val));
								context_data_val = 0;
							} else {
								context_data_position++;
							}
							value = value >> 1;
						}
					} else {
						value = 1;
						for (i = 0; i < context_numBits; i++) {
							context_data_val = (context_data_val << 1) | value;
							if (context_data_position == bitsPerChar - 1) {
								context_data_position = 0;
								context_data.append(getCharFromInt.doFunc(context_data_val));
								context_data_val = 0;
							} else {
								context_data_position++;
							}
							value = 0;
						}
						value = context_w.charAt(0);
						for (i = 0; i < 16; i++) {
							context_data_val = (context_data_val << 1) | (value & 1);
							if (context_data_position == bitsPerChar - 1) {
								context_data_position = 0;
								context_data.append(getCharFromInt.doFunc(context_data_val));
								context_data_val = 0;
							} else {
								context_data_position++;
							}
							value = value >> 1;
						}
					}
					context_enlargeIn--;
					if (context_enlargeIn == 0) {
						context_enlargeIn = powerOf2(context_numBits);
						context_numBits++;
					}
					context_dictionaryToCreate.remove(context_w);
				} else {
					value = context_dictionary.get(context_w);
					for (i = 0; i < context_numBits; i++) {
						context_data_val = (context_data_val << 1) | (value & 1);
						if (context_data_position == bitsPerChar - 1) {
							context_data_position = 0;
							context_data.append(getCharFromInt.doFunc(context_data_val));
							context_data_val = 0;
						} else {
							context_data_position++;
						}
						value = value >> 1;
					}

				}
				context_enlargeIn--;
				if (context_enlargeIn == 0) {
					context_enlargeIn = powerOf2(context_numBits);
					context_numBits++;
				}
				// Add wc to the dictionary.
				context_dictionary.put(context_wc, context_dictSize++);
				context_w = context_c;
			}
		}
		
		// Output the code for w.
		if (!context_w.isEmpty()) {
			if (context_dictionaryToCreate.contains(context_w)) {
				if (context_w.charAt(0) < 256) {
					for (i = 0; i < context_numBits; i++) {
						context_data_val = (context_data_val << 1);
						if (context_data_position == bitsPerChar - 1) {
							context_data_position = 0;
							context_data.append(getCharFromInt.doFunc(context_data_val));
							context_data_val = 0;
						} else {
							context_data_position++;
						}
					}
					value = context_w.charAt(0);
					for (i = 0; i < 8; i++) {
						context_data_val = (context_data_val << 1) | (value & 1);
						if (context_data_position == bitsPerChar - 1) {
							context_data_position = 0;
							context_data.append(getCharFromInt.doFunc(context_data_val));
							context_data_val = 0;
						} else {
							context_data_position++;
						}
						value = value >> 1;
					}
				} else {
					value = 1;
					for (i = 0; i < context_numBits; i++) {
						context_data_val = (context_data_val << 1) | value;
						if (context_data_position == bitsPerChar - 1) {
							context_data_position = 0;
							context_data.append(getCharFromInt.doFunc(context_data_val));
							context_data_val = 0;
						} else {
							context_data_position++;
						}
						value = 0;
					}
					value = context_w.charAt(0);
					for (i = 0; i < 16; i++) {
						context_data_val = (context_data_val << 1) | (value & 1);
						if (context_data_position == bitsPerChar - 1) {
							context_data_position = 0;
							context_data.append(getCharFromInt.doFunc(context_data_val));
							context_data_val = 0;
						} else {
							context_data_position++;
						}
						value = value >> 1;
					}
				}
				context_enlargeIn--;
				if (context_enlargeIn == 0) {
					context_enlargeIn = powerOf2(context_numBits);
					context_numBits++;
				}
				context_dictionaryToCreate.remove(context_w);
			} else {
				value = context_dictionary.get(context_w);
				for (i = 0; i < context_numBits; i++) {
					context_data_val = (context_data_val << 1) | (value & 1);
					if (context_data_position == bitsPerChar - 1) {
						context_data_position = 0;
						context_data.append(getCharFromInt.doFunc(context_data_val));
						context_data_val = 0;
					} else {
						context_data_position++;
					}
					value = value >> 1;
				}

			}
			context_enlargeIn--;
			if (context_enlargeIn == 0) {
				context_enlargeIn = powerOf2(context_numBits);
				context_numBits++;
			}
		}

		// Mark the end of the stream
		value = 2;
		for (i = 0; i < context_numBits; i++) {
			context_data_val = (context_data_val << 1) | (value & 1);
			if (context_data_position == bitsPerChar - 1) {
				context_data_position = 0;
				context_data.append(getCharFromInt.doFunc(context_data_val));
				context_data_val = 0;
			} else {
				context_data_position++;
			}
			value = value >> 1;
		}

		// Flush the last char
		while (true) {
			context_data_val = (context_data_val << 1);
			if (context_data_position == bitsPerChar - 1) {
				context_data.append(getCharFromInt.doFunc(context_data_val));
				break;
			}
			else
				context_data_position++;
		}
		return context_data.toString();
	}
	
	private static abstract class DecompressFunctionWrapper {
		public abstract char doFunc(int i);
	}
	protected static class DecData {
		public char val;
		public int position;
		public int index;		
	}

	public static String f(int i) {
		return String.valueOf((char) i);
	}
	public static char fc(int i) {
		return (char) i;
	}

	public static String decompress(final String compressed) {
		if (compressed == null)
			return "";
		if (compressed.isEmpty())
			return null;
		return LZString._decompress(compressed.length(), 32768, new DecompressFunctionWrapper() {
			@Override
			public char doFunc(int i) {
				return compressed.charAt(i);
			}
		});
	}
	private static String _decompress(int length, int resetValue, DecompressFunctionWrapper getNextValue) {
		List<String> dictionary = new ArrayList<String>();
		// TODO: is next an unused variable in original lz-string?
		@SuppressWarnings("unused")
		int next;
		int enlargeIn = 4;
		int dictSize = 4;
		int numBits = 3;
		String entry = "";
		StringBuilder result = new StringBuilder();
		String w;
		int bits, resb; int maxpower, power;
		String c = null;
		DecData data = new DecData();
		data.val = getNextValue.doFunc(0);
		data.position = resetValue;
		data.index = 1;
		
		for (int i = 0; i < 3; i += 1) {
			dictionary.add(i, f(i));
		}
		
		bits = 0;
		maxpower = (int) powerOf2(2);
		power = 1;
		while (power != maxpower) {
			resb = data.val & data.position;
			data.position >>= 1;
			if (data.position == 0) {
				data.position = resetValue;
				data.val = getNextValue.doFunc(data.index++);
			}
			bits |= (resb > 0 ? 1 : 0) * power;
			power <<= 1;
		}
		
	    switch (next = bits) {
	      case 0:
	          bits = 0;
	          maxpower = (int) powerOf2(8);
	          power=1;
	          while (power != maxpower) {
	            resb = data.val & data.position;
	            data.position >>= 1;
	            if (data.position == 0) {
	              data.position = resetValue;
	              data.val = getNextValue.doFunc(data.index++);
	            }
	            bits |= (resb>0 ? 1 : 0) * power;
	            power <<= 1;
	          }
	        c = f(bits);
	        break;
	      case 1:
	          bits = 0;
	          maxpower = powerOf2(16);
	          power=1;
	          while (power!=maxpower) {
	            resb = data.val & data.position;
	            data.position >>= 1;
	            if (data.position == 0) {
	              data.position = resetValue;
	              data.val = getNextValue.doFunc(data.index++);
	            }
	            bits |= (resb>0 ? 1 : 0) * power;
	            power <<= 1;
	          }
	        c = f(bits);
	        break;
	      case 2:
	        return "";
	    }
	    dictionary.add(3, c);
	    w = c;
		result.append(w);
	    while (true) {
	        if (data.index > length) {
	          return "";
	        }

	        bits = 0;
	        maxpower = powerOf2(numBits);
	        power=1;
	        while (power!=maxpower) {
	          resb = data.val & data.position;
	          data.position >>= 1;
	          if (data.position == 0) {
	            data.position = resetValue;
	            data.val = getNextValue.doFunc(data.index++);
	          }
	          bits |= (resb>0 ? 1 : 0) * power;
	          power <<= 1;
	        }
	        // TODO: very strange here, c above is as char/string, here further is a int, rename "c" in the switch as "cc"
	        int cc;
	        switch (cc = bits) {
	          case 0:
	            bits = 0;
	            maxpower = powerOf2(8);
	            power=1;
	            while (power!=maxpower) {
	              resb = data.val & data.position;
	              data.position >>= 1;
	              if (data.position == 0) {
	                data.position = resetValue;
	                data.val = getNextValue.doFunc(data.index++);
	              }
	              bits |= (resb>0 ? 1 : 0) * power;
	              power <<= 1;
	            }

	            dictionary.add(dictSize++, f(bits));
	            cc = dictSize-1;
	            enlargeIn--;
	            break;
	          case 1:
	            bits = 0;
	            maxpower = powerOf2(16);
	            power=1;
	            while (power!=maxpower) {
	              resb = data.val & data.position;
	              data.position >>= 1;
	              if (data.position == 0) {
	                data.position = resetValue;
	                data.val = getNextValue.doFunc(data.index++);
	              }
	              bits |= (resb>0 ? 1 : 0) * power;
	              power <<= 1;
	            }
	            dictionary.add(dictSize++, f(bits));
	            cc = dictSize-1;
	            enlargeIn--;
	            break;
	          case 2:
	        	return result.toString();
			}

	        if (enlargeIn == 0) {
	          enlargeIn = powerOf2(numBits);
	          numBits++;
	        }

	        if (cc < dictionary.size() && dictionary.get(cc) != null) {
	        	entry = dictionary.get(cc);
	        } else {
	          if (cc == dictSize) {
	            entry = w + w.charAt(0);
	          } else {
	            return null;
	          }
	        }
	        result.append(entry);

	        // Add w+entry[0] to the dictionary.
	        dictionary.add(dictSize++, w + entry.charAt(0));
	        enlargeIn--;

	        w = entry;

	        if (enlargeIn == 0) {
	          enlargeIn = powerOf2(numBits);
	          numBits++;
	        }

	      }
		
	}

	private static int powerOf2(int power) {
	    return 1 << power;
    }
	
	public static void main(String[] args) {
		String input;
//		input = "hello";
		input = "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";
		System.out.println(decompressFromBase64(input));
	
//		System.out.println(decompress(compress(input)));
//		System.out.println(decompressFromBase64(compressToBase64(input)));
//		System.out.println(decompressFromUTF16(compressToUTF16(input)));
//		System.out.println(decompressFromEncodedURIComponent(compressToEncodedURIComponent(input)));
	}

	public void test() {
		System.out.println(decompressFromBase64("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"));
	}
}
