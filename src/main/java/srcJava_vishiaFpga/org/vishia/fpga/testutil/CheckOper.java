package org.vishia.fpga.testutil;

public class CheckOper {
  /**Helper class for check.
   * @author hartmut
   *
   */
  public static class CharMinMax {
    char cc; int min; int max;
  
    public CharMinMax(char cc, int min, int max) {
      this.cc = cc;
      this.min = min;
      this.max = max;
    }
  }
  
  
  public static String checkOutput(CharSequence cs, int startPos, CharMinMax[] checks) {
    char cx = '\0';

    boolean bStartRange = true;
    int nrChars = 0;
    CharMinMax  checkCurr = null;
    for(int pos = startPos; pos < cs.length(); ++pos) {
      char cc = cs.charAt(pos);
      if(cc !=cx) {
        boolean bFound = false;
        for(CharMinMax check: checks) {
          if(cc == check.cc) {
            bFound = true;
            if(! bStartRange) {
              if(nrChars < checkCurr.min ) {
                return "@" + pos + ": too short: " + nrChars;
              }
              if(nrChars > checkCurr.max ) {
                return "@" + pos + ": too long: " + nrChars;
              }
            }
            checkCurr = check;
            bFound = true;
            break;
          }
        }
        if(!bFound) {
          return "@" + pos + ": faulty char: " + cc;
        }
        bStartRange = cx ==0;
        cx = cc;
        nrChars = 1;
      } else {
        nrChars +=1;
      }
    }
    return null;
  }


}
