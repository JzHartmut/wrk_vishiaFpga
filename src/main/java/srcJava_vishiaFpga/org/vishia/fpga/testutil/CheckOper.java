package org.vishia.fpga.testutil;


/**This class contains some static operations to check results especially for {@link org.vishia.util.TestOrg}
 * to get true or false
 * @author Hartmut Schorrig, LPGL 2.1 License
 *
 */
public class CheckOper {

  
  
  
  /**Version, history and license.
   * Changes:
   * <ul>
   * <li>2022-06-03 Hartmut: created
   * </ul>
   * 
   * 
   * <b>Copyright/Copyleft</b>:
   * For this source the LGPL Lesser General Public License,
   * published by the Free Software Foundation is valid.
   * It means:
   * <ol>
   * <li> You can use this source without any restriction for any desired purpose.
   * <li> You can redistribute copies of this source to everybody.
   * <li> Every user of this source, also the user of redistribute copies
   *    with or without payment, must accept this license for further using.
   * <li> But the LPGL is not appropriate for a whole software product,
   *    if this source is only a part of them. It means, the user
   *    must publish this part of source,
   *    but don't need to publish the whole source of the own product.
   * <li> You can study and modify (improve) this source
   *    for own using or for redistribution, but you have to license the
   *    modified sources likewise under this LGPL Lesser General Public License.
   *    You mustn't delete this Copyright/Copyleft inscription in this source file.
   * </ol>
   * If you are intent to use this sources without publishing its usage, you can get
   * a second license subscribing a special contract with the author. 
   * 
   * @author Hartmut Schorrig = hartmut.schorrig@vishia.de
   * 
   */
  public final static String sVersion = "2022-06-06";

  /**Helper class for check.
   * Instances of this class can be used for example:
   * <pre>
    CheckOper.CharMinMax[] checkLedA = { new CheckOper.CharMinMax('_', 200, 9999), new CheckOper.CharMinMax('A', 200, 300)};
   * </pre>
   * It means that the test array expects the character '_' at least for 200 steps, and 'A' from 200..300 steps one after another.
   * See {@link CheckOper#checkOutput(CharSequence, int, CharMinMax[])}.
   */
  public static class CharMinMax {
    char cc; int min; int max;
  
    public CharMinMax(char cc, int min, int max) {
      this.cc = cc;
      this.min = min;
      this.max = max;
    }
  }
  
  
  /**Checks whether defined character are found in the output string cs.
   * Example:<pre>
   * module.signal______:__BBBBBBBBBB________AAAAAAAABBBBBBBBBBBB___
   * </pre>
   * The number of occurence (argument checks) should set to:
   * <pre>
    CheckOper.CharMinMax[] checkLedA = { new CheckOper.CharMinMax('_', 8, 8), new CheckOper.CharMinMax('A', 8, 8), new CheckOper.CharMinMax('B', 10, 12)};
   * </pre>
   * <ul>
   * <li>The first chars after startPos = 20 are not checked, because the period may be faulty.
   * <li>The last chars on end are also not checked, because the period may not be complete, aborted or finished simulation. 
   * <li>Elsewhere it is asserted that A is at least 8 steps, etc.  
   * </ul>
   * @param cs May be output as line output 
   * @param startPos all character before are not checked, usual for the moudule.signal name 
   *  after startPos the first position which is checked is the change to another character starting from startPos.
   * @param check Array of all occurrences of characters in cs from startPos. It should be complete.
   * @return null if proper, a readable error message if false. 
   */
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
