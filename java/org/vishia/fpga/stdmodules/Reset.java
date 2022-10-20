package org.vishia.fpga.stdmodules;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;
import org.vishia.fpga.testutil.StateStoreFpga;

//tag::classdef[]
public class Reset implements FpgaModule_ifc, Reset_ifc {
//end::classdef[]

  /**Version, history and license.
   * <ul>
   * <li>2022-06-11 The accesses for stubFalse and stubTrue are added here, often used, and this module is also often used. 
   *   TODO Better create a specfic Module only for stubs.
   * <li>2022-05- Hartmut created.
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
  public final static String sVersion = "2022-06-11"; 

  private static final class Ref {
    final Reset_Inpin_ifc resetInp;
    
    public Ref(Reset_Inpin_ifc resetInp) {
      this.resetInp = resetInp;
    }
  }
  
  final Ref ref;
  
  /**Inner PROCESS class builds a TYPEDEF RECORD in VHDL and a PROCESS for each (usual one) instance.
   * Note: It can be private, access via interface.
   */
  @Fpga.VHDL_PROCESS private static final class Q{

    @Fpga.STDVECTOR(4) final int resetCount;

    /**This is the variable of the record accessed from outside, but access via interface.
     * Hence it can be private (package private but the class is private). 
     */
    final boolean res;
    
    final int time;
    
    Q() {
      this.resetCount = 0;
      this.res = false;
      this.time = 0;
    }
    
    @Fpga.VHDL_PROCESS Q(int time, Q z, Ref ref) {
      Fpga.checkTime(time, z.time, 1);                 // need be a fast logic
      this.time = time;
      if(ref.resetInp.reset_Pin() == false) {              // lo active clear pin
        this.resetCount = 0b0000;                
      }
      else if(z.res) {
        this.resetCount = z.resetCount +1;
      }
      else {
        this.resetCount = z.resetCount;
      }
      this.res = z.resetCount < 0b1110;                  // hi active internal clear signal if clrCount = 0...13
    }

    
  }

  
  
  protected Q q = new Q();
  private Q d_q;
  
  
  /**Module constructor with public access to instantiate.
   * <br>
   * Note: The arguments should have the exact same name and type as in the {@link Ref#Ref(Reset_ifc, ClockDivider)} inner class.
   * @param resetInp this should be immediately the inputFpga port block.
   */
  public Reset(Reset_Inpin_ifc resetInp) {
    this.ref = new Ref(resetInp);
  }
  
  
  //public Clr_ifc q ( ) {return this.q; }
  
  @Override
  public void step(int time) {
    this.d_q = new Q(time, this.q, this.ref);
  }

  @Override
  public void update() {
    this.q = this.d_q;
  }

  //tag::reset()[]
  @Override public boolean reset ( int time, int max) { return this.q.res; }
  //end::reset()[]
  
  //tag::Store[]
  /**Stores the state for special tests.
   * You can use this implementation as template for your modules.
   */
  public static class Store extends StateStoreFpga < Reset > {
    final Q q;

    /**Creates a Store instance, which refers the data from the {@link Reset#q} instance,
     * it is the PROCESS data, able to call after a defined simulation procedure,
     * to resume later exact from this state.
     * @param time The time stamp of the simulation
     * @param src The reference to the module.
     */
    public Store(int time, Reset src) {
      super(time, src);
      this.q = src.q;
    }

    /**Restore the state to the same module, which is used on creation.
     * It is presumed that the {@link Reset#q} instance was not changed meanwhile.
     * However, this is guaranteed if the Application Pattern Style Guide is followed,
     * and also because all members in {@link Reset.Q} are <code>final</code>.
     */
    @Override public int restore() {
      super.dst.q = this.q;
      return super.time;
    }
  }
  //end::Store[]


  /**This is a stub to return always false if a Bit_ifc is required. 
   * It is associated to the Reset module because this module is usual in use. 
   * TODO maybe also support a static ifc-implementation. Then it can be associated in the Bit_ifc itself.
   */
  public @Fpga.IfcAccess Bit_ifc stubFalse = new Bit_ifc() {
    @Override public boolean getBit () { return false; } 
  };

  /**This is a stub to return always false if a Bit_ifc is required. 
   * It is associated to the Reset module because this module is usual in use. 
   * TODO maybe also support a static ifc-implementation. Then it can be associated in the Bit_ifc itself.
   */
  public @Fpga.IfcAccess Bit_ifc stubTrue = new Bit_ifc() {
    @Override public boolean getBit () { return true; } 
  };

  /**This is a stub to return always a value of 0 . 
   * It is associated to the Reset module because this module is usual in use. 
   * TODO maybe also support a static ifc-implementation. Then it can be associated in the Bit_ifc itself.
   */
  public @Fpga.IfcAccess Word_ifc stubWord16 = new Word_ifc() {
    @Override public int getWord( int time, int min ) { return 0x0000; }
  };


  
  
}
