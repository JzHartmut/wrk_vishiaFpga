package org.vishia.fpga;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.vishia.util.Debugutil;

/**This class contains hardware related opeations for FPGA simulation
 * which are also accepted by the Java2Vhdl translator.
 * @author hartmut Schorrig
 *
 */
public class Fpga {
  
  /**Version, history and license.
   * <ul>
   * <li>2022-05-02 {@link #setBits(int, int, int, int)} without 5th argument.
   * <li>2022-02-14 Hartmut created.
   * </ul>
   * <br><br>
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
   */
  public final static String sVersion = "2022-02-08"; 

  /**Gets one bit from a vector. Vhdl: vector(7)
   * @param vector variable which presents a Vhdl vector
   * @param bit bit number set in vector output in the return value, should be a simple const number.
   * @param value the value to set to this bits. An ordinary expression of bool type.
   *   On VHDL an automatic conversion to STD_VECTOR will be done if necessary. 
   */
  public static int setBit(int vector, int bit, boolean value) { 
    int mask = 1<<bit;
    if(value) { return vector | mask; } else { return vector & ~mask; }
  }

  
  /**Replaces the dedicated bits with value. VHDL: vector(16 DOWNTO 1) <= value(15 DOWNTO 0)
   * @param vector the given vector, for the unchanged bits in return, current value.
   * @param dstMSB most significant bit set in vector output in the return value, should be a simple const number.
   * @param dstLSB last significant bit set in vector output in the return value, should be a simple const number.
   * @param value the value to set to this bits, the same number of bits are used. An ordinary expression. 
   * @return changed vector
   */
  public static int setBits(int vector, int dstMSB, int dstLSB, int value) { 
    int mask = ( (1<<(dstMSB-dstLSB+1)) -1) << dstLSB;
    return (vector & ~mask) | ((value<< dstLSB) & mask);  //replace the dedicated bits with value (right aligned)
  }

  
  /**Gets one bit from a vector. Vhdl: vector(7)
   * @param vector variable which presents a Vhdl vector
   * @param bit the bit, from 0
   * @return boolean meaning of bit.
   */
  public static boolean getBit(int vector, int bit) { 
    int mask = 1<<bit;
    return (vector & mask)!=0 ? true: false; 
  }

  
  /**Shifts a vector 1 to left and replaces the bit 0 with low value.
   * In Vhdl it is vector(14 DOWNTO 0) & low
   * @param vector the previous state (z)
   * @param nrBits valid number of bits to shift
   * @param low It should present a boolean for a Vhdl: BIT
   * @return new value.
   */
  public static int getBitsShL(int vector, int nrBits, boolean low) {
    int mask = ((1<<nrBits)-1);
    return ((vector & mask) << 1) | (low ? 1 : 0); 
  }
  
  /**Shifts a vector 1 to right and replaces the ms bit  with high value.
   * In Vhdl it is high & vector(15 DOWNTO 1)
   * @param high most significant bit to replace. It should present a boolean for a Vhdl: BIT
   * @param bitHigh bit position for high or number of bits to shift for vector. vector is used with (bithigh downto 1)
   * @param vector
   * @return new value.
   */
  public static int getBitsShR(boolean high, int bitHigh, int vector) {
    int mask = 1<< bitHigh ;  //0x8000 on bitHigh = 15
    return (high ? mask : 0) | ((vector >> 1) & (mask-1)); 
  }
  
  /**Returns in VHDL vector(6 DOWNTO 2) if msp=6 and lsb=2.
   * In Java returns the bits msb...lsb right aligned.
   * @param vector
   * @param msb the msb bit itself inclusively
   * @param lsb the lsb inclusively.
   * @return
   */
  public static int getBits(int vector, int msb, int lsb) {
    int mask = (( 1<< (msb-lsb+1) ) -1); // & ~((1<<lsb)-1);
    return (vector >> lsb) &mask; 
  }
  
  
  /**Concatenate two bit vectors
   * @param high
   * @param bitPos Position where high should be placed in result. 
   *   This should be exact the length of low, checked in assertion. 
   * @param low
   * @return
   */
  public static int concatbits(int high, int bitPos, int low) {
    assert( (low & ~((1<<bitPos)-1)) ==0);  //high bits from low are all 0.
    return (high << bitPos) | (low & ((1<<(bitPos))-1));
  }
  
  
  
  public static void measTime(int[] tgroup, int ixGroup, int tdiff) {
    if(tgroup[ixGroup] > tdiff) { tgroup[ixGroup] = tdiff; }
  }
  
  
  
  /**Checks the size of a vector adequate to its annotation
   * @param inClass
   * @param var The field in this class.
   * @param nrBits
   * @return
   */
  public static boolean checkVector(Class<?> inClass, String var, int nrBits) {
    try {
      Field element = inClass.getDeclaredField(var);
      Fpga.BITVECTOR[] annoBitvector = element.getAnnotationsByType(Fpga.BITVECTOR.class);
      if(annoBitvector.length>=1 && annoBitvector[0].value() == nrBits) {
        return true;
      }
      Fpga.STDVECTOR[] annoStdvector = element.getAnnotationsByType(Fpga.STDVECTOR.class);
      if(annoStdvector.length>=1 && annoStdvector[0].value() == nrBits) {
        return true;
      }
    } catch (NoSuchFieldException | SecurityException e) {
      return false;
    }
    return false;
  }
  
  
  
  /**Check the time. This operation is used to generate timing constrains, 
   * and also used in simulation for check the time.
   * @param time current time of access
   * @param ztime time of the accessed variable
   * @param min minimal difference.
   */
  public static void checkTime(int time, int ztime, int min) {
    if(time==0 || min ==0) return;  //no check yet. 
    assert(time > ztime);           //detect errors with faulty call, 
    if(time - ztime < min) {
      Debugutil.stop();
      
      //TODO set breakpoint, or conditional invoke a test assertion. 
    }
  }
  
  
  public static void checkDbg(boolean cond) {
    if(cond) {
      Debugutil.stop();
    }
  }
  
  
  
  /**Defines that an input pin of the Fpga or of an module should be clocked immediately. 
   * Only the clocked signal is available. 
   * If both, the non clocked and the clocked input should be used, then the FF for clock should be a part of a module.
   * Generally non clocked and clocked signals should only used for one specific logic part.
   */
  public @interface ClockedInput{  }

  /**Defines that an Operation is existing in the module class to access data from  RECORD instance (associated to a PROCESS).
   */
  public @interface GetterVhdl{  }

  /**Defines a sub module in another module which is only responsible to implement an interface
   * as access point to the containing module. */
  public @interface IfcAccess{  }

  /**Defines an numeric variable in VHDL as BIT_VECTOR(<value-1> DOWNTO 0)
   * This allows only specific routines in this class or assignments.
   * Arithmetic operations are not supported.
   */
  public @interface BITVECTOR{ int value(); }

  /**Defines an numeric variable in VHDL as STD_LOGIC_VECTOR(<value-1> DOWNTO 0)
   * This is necessary if numeric operations are done.
   */
  public @interface STDVECTOR{ int value(); }

  /**Defines an numeric variable in VHDL as STD_LOGIC_VECTOR(<value-1> DOWNTO 0)
   * This is necessary if numeric operations are done.
   */
  public @interface VHDL_PROCESS{  }

  
}
