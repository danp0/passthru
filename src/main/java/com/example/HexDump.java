package com.example;

import java.util.LinkedList;
import java.util.List;

/**
 * HexDump
 */
public class HexDump {
  private LinkedList<String> hex;
  private LinkedList<String> chr;

  public HexDump() {
  }

  public void dump(byte[] bytes) {
    hex = new LinkedList<String>();
    chr = new LinkedList<String>();
    StringBuilder sbhex = null;
    StringBuilder sbchr = null;
    for (int i = 0; i < bytes.length; ++i) {
      if (i % 16 == 0) {
        if (i != 0) {
          hex.add(sbhex.toString());
          chr.add(sbchr.toString());
        }
        sbhex = new StringBuilder();
        sbchr = new StringBuilder();
      } else {
        sbhex.append(" ");
      }
      sbhex.append(String.format("%02X", bytes[i]));
      if (Character.isISOControl((char)bytes[i])) {
        sbchr.append(".");
      } else {
        sbchr.append((char)bytes[i]);
      }
    }
    if (sbhex != null && sbhex.length() > 0) {
      hex.add(sbhex.toString());
    }
    if (sbchr != null && sbchr.length() > 0) {
      chr.add(sbchr.toString());
    }
  }

  public List<String> getChr() {
    return chr;
  }

  public List<String> getHex() {
    return hex;
  }
}
