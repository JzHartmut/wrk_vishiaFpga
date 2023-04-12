package org.vishia.fpga.tmpl_J2Vhdl;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;
import org.vishia.fpga.stdmodules.Bit_ifc;
import org.vishia.fpga.stdmodules.CeTime_ifc;
import org.vishia.fpga.stdmodules.Reset_ifc;

public final class ModuleXY implements FpgaModule_ifc {                         // (1) <tag::classModuleXY[]>

  public static class In {                                                      // (2)
    boolean in1;
    @Fpga.BITVECTOR(8)   int inVal;
  }
  public final In in = new In();
  
  public static class Out {                                                     // (3)
    public CeTime_ifc time_ce0, time_ce7;
    boolean out1, out2, out3, out4, out5, out6, out7;
    char olg1, olg5;
    @Fpga.BITVECTOR(16) int bVal1, bVal2, bVal3;
    @Fpga.STDVECTOR(16) int oVal1, oVal2, oVal3, oVal4, oVal5, oVal6, oVal7, oVal8, oVal9;
    @Fpga.STDVECTOR(16) int oPins; 
    @Fpga.BITVECTOR(8) public int oVec81, oVec82, oVec83, oVec84, oVec85
    , oVec86, oVec87, oVec88, oVec89;
  }
  public final Out out = new Out();
  //
  static class Ref {  // This class contains references to other modules        // (4)
    Reset_ifc reset;
    OtherModule_ifc mdlXx;
    MyFpgaIo ioPins;
    Ref ( Reset_ifc reset, OtherModule_ifc mdlXx, MyFpgaIo ioPins ){            // (5)
      this.reset = reset; this.mdlXx = mdlXx; 
      this.ioPins = ioPins;                                                     // (5a)
    }
  }
  private Ref ref;                                                              // (6)
  //
  static class Modules {        // This class contains sub modules              // (7)
    final SubmoduleXz submdlXz = new SubmoduleXz();                             // (8)
    Modules ( Ref ref, ModuleXY thism ) {                                       // (9)
      this.submdlXz.init(ref.reset);
    }
  }
  Modules modules;                                                              // (10) </tag::classModuleXY[]>

  
  
  /**A process builds the ce signal, true for 1 of 10 clock times.
   */
  @Fpga.VHDL_PROCESS public static class PCE {
    final boolean ce, ce7;
    final @Fpga.STDVECTOR(4) int ct;
    final int time_ce, time_ce7;
    PCE(){
      this.ce = this.ce7 = false; this.ct = 0;
      this.time_ce = this.time_ce7 = 0;
    }
    @Fpga.VHDL_PROCESS PCE(int time, PCE z, Ref ref, ModuleXY thism) {
      if(Fpga.getBits(z.ct, 3,2) == 0b11) {
        this.ct = 0b1000;            // counts 8..0,-1, 10 times.
        this.ce = true;
        this.time_ce = Fpga.checkTime(time, z.time_ce, thism.srcCE0.period());  // checks the own period. 
      } else {
        this.ct = z.ct -1;
        this.time_ce = z.time_ce;
        this.ce = false;
      }
      if(z.ct == 0b0010) {
        this.ce7 = true;
        this.time_ce7 = Fpga.checkTime(time, z.time_ce, thism.srcCE7.period());  // checks the own period. 
      } else {
        this.ce7 = false;
        this.time_ce7 = z.time_ce7;
      }
    }
  }
  protected PCE pCE = new PCE(); private PCE pCE_d;
  
  
  /**This is a simple public access to one immeditely FlipFlop in this module.
   * It is the ce, which enables eacht 10th time.
   * @return true any 10th CLK
   */
  @Fpga.IfcAccess public CeTime_ifc srcCE0 = new CeTime_ifc() {
    @Override public boolean ce () { return ModuleXY.this.pCE.ce; }

    @Override public int time () {
      return ModuleXY.this.pCE.time_ce;
    }

    @Override public String timeGroupName () { return "CE0"; }

    @Override public int period () { return 10; }
    
  };
  
  
  @Fpga.IfcAccess public CeTime_ifc srcCE7 = new CeTime_ifc() {
    @Override public boolean ce ( ) { return ModuleXY.this.pCE.ce7; }

    @Override public int time () {
      return ModuleXY.this.pCE.time_ce7;
    }
    @Override public String timeGroupName () { return "CE7"; }

    @Override public int period () { return 10; }
    
};
  

  

  
  @Fpga.VHDL_PROCESS public static class Q_CE0 {             // (1)   <tag::Q_CE0><tag::Q_CE0_time>
    public final CeTime_ifc time_;                           // (2)   </tag::Q_CE0_time>
    final boolean var1;                                      // (3)
    @Fpga.STDVECTOR(16) public final int val;
    Q_CE0() {                                                // (4)
      this.time_ = null;
      this.var1 = false;
      this.val = 0x0000;
    }
    @Fpga.VHDL_PROCESS  Q_CE0(int time, Q_CE0 z, Ref ref, ModuleXY thism) {    // (5)
      this.time_ = thism.srcCE0;                     // (6)
      if(thism.srcCE0.ce()) {                        // (7)
        if(ref.reset.res(time, 1)){                  // (8)
          this.var1 = false;
          this.val = 0x1234;                         // (9)
        } else {                                     // (10)
          this.val = z.val + 1;                      // (11)
          this.var1 = (z.val == 0x0010);
        }
      } else throw new IllegalStateException();      // (12)
    }
  }
  protected Q_CE0 q_CE0 = new Q_CE0();                  // (13)
  private Q_CE0 q_CE0_d;                             // (14)    </tag::Q_CE0>



  @Fpga.VHDL_PROCESS public static class Val_CE7 {             // (1)
    final CeTime_ifc time_;
    final boolean bit1, bit2, bit3;                                  // (2)
    final @Fpga.STD_LOGIC boolean lg1, lg2, lg3;                                  // (2)
    final char lc1, lc2, lc3;
    @Fpga.STDVECTOR(16) public final int val1, val2, val3;
    @Fpga.BITVECTOR(16) public final int bvec1, bvec2;
    Val_CE7() {                                                // (3)
      this.time_ = null;
      this.bit1 = this.bit2 = this.bit3 = false;
      this.lg1 = this.lg2 = this.lg3 = false;
      this.lc1 = this.lc2 = this.lc3 = '0';
      this.val1 = this.val2 = this.val3 = 0x0000;
      this.bvec1 = this.bvec2 = 0x0000;
    }
    @Fpga.VHDL_PROCESS  Val_CE7(int time, Val_CE7 z, Ref ref, ModuleXY thism) {    // (4)         //tagline:processValExmpl[ident=0]
      boolean localVar1 = ref.ioPins.input.reset_Pin;                                     //tagline:localVarExmpl[ident=2]
      this.time_ = thism.srcCE7;
      if(thism.srcCE7.ce()) {
        if(ref.reset.res(time, 10)){                         // (5)
          //this.bit1 = this.bit2 = this.bit3 = false;  //not supported
          localVar1 = false;
          this.bit1 = false;
          this.bit2 = false; this.bit3 = false;
          this.lg1 = false; this.lg2 = false; this.lg3 = false;
          this.lc1 = '0'; this.lc2 = '0'; this.lc3 = '0'; 
          this.val1 = 0x0000; this.val2 = 0x0000; this.val3 = 0x0000;
          this.bvec1 = 0x0000; this.bvec2 = 0x0000;
        } else {
          this.bit1 = z.bit1 & localVar1 & ref.mdlXx.getSpecValue(time, 1);                   //tag:operandsExmpl[indent=2]
          this.bit2 = thism.pCE.ce; 
          this.val1 = ref.mdlXx.getConstValue();                                              //tagend:operandsExmpl[]
          this.bit3 = z.bit3;
          this.lg1 = z.bit1 & z.lg1;
          this.lg2 = z.lg2 & z.lg3;
          this.lg3 = z.lg2 & z.lg3;
          this.lc1 = z.bit1 && z.lg1 ? '1' : '0';
          this.lc2 = z.bit1 && z.lg1 ? '1' : '0';
          this.lc3 = z.bit1 && z.lg1 ? '1' : '0';
          Fpga.checkTime(time, thism.q_CE0.time_, 7);    //because of access there
          this.val2 = ref.mdlXx.getSpecValue(time, 1) ? 0xfedc : thism.q_CE0.val;
          this.val3 = ~ z.val2;
          this.bvec1 = z.bvec1 | z.bvec2;
          this.bvec2 = ref.mdlXx.getSpecValue(time,1) ? 0x1248 : 0x248c;
        }
      } else {
        this.lg1 = z.lg1;
        throw new IllegalArgumentException("should only call with ce");
      }
    }
  }
  protected Val_CE7 val_CE7 = new Val_CE7();        // (10)
  private Val_CE7 val_CE7_d;                        // (11)




  @Fpga.IfcAccess Bit_ifc getValxy = new Bit_ifc() {                                //(1)
    @Override public boolean getBit () {                                            //(2) 
      return ModuleXY.this.q_CE0.var1;                                                  //(3)
    }
  };
  
  
  public ModuleXY ( Reset_ifc reset, OtherModule_ifc mdlXx, MyFpgaIo ioPins ) {     //<1>
    this.ref = new Ref(reset, mdlXx, ioPins);                                       //<2>
    this.modules = new Modules( this.ref, this);
  }

  public ModuleXY ( ) { }       // use init to initialize                           //<3>
    
  public void init ( Reset_ifc reset, OtherModule_ifc mdlXx, MyFpgaIo ioPins ) {    //<4>
    this.ref = new Ref(reset, mdlXx, ioPins);
    this.modules = new Modules( this.ref, this);
  }

    @Override public void reset ( ) { // call of the empty ctor for all process inner classes  <tag::stepUpd[]>
      this.q_CE0 = new Q_CE0();     // to set hardware reset values                         <5>
      this.modules.submdlXz.reset();
    }

    public void input ( ) { // optional sets input records of sub modules           <6>
      this.modules.submdlXz.in.var = this.ref.mdlXx.getSpecValue(0,0);
    }

    @Override public void step ( int time ) {    // calculates the D-states (pre states)      <7>
      this.pCE_d = new PCE(time, this.pCE, this.ref, this);
      if(this.srcCE0.ce()) {
        this.q_CE0_d = new Q_CE0(time, this.q_CE0, this.ref, this);
      }
      if(this.srcCE7.ce()) {
        this.val_CE7_d = new Val_CE7(time, this.val_CE7, this.ref, this);
      }
      this.modules.submdlXz.step(time);
    }

    @Override public void update ( ) {           // Activates the Q-states (Flipflop outputs)
      this.q_CE0 = this.q_CE0_d;               // this is the clock edge in hardware        <8>
      this.pCE = this.pCE_d;
      this.val_CE7 = this.val_CE7_d;
      this.modules.submdlXz.update();
    }

    public void output ( ) {   // optional, but necessary for top level             <9>
      this.out.time_ce0 = this.q_CE0.time_;       // combinatoric only from this signals.   </tag::stepUpd[]>
      this.out.time_ce7 = this.val_CE7.time_;       // combinatoric only from this signals.
      this.out.out1 = this.q_CE0.var1 && this.modules.submdlXz.out.outX1;
      this.ref.ioPins.output.testOutFromModule = true;
      //this.out.out1 = this.val.bit1  & this.val.bit2;
      this.out.out2 = this.val_CE7.bit3 && this.val_CE7.bit2;
      this.out.out3 = this.val_CE7.lg1 && this.val_CE7.bit2;
      this.out.olg1 = this.val_CE7.lg1 && this.val_CE7.bit2 ? '1' : '0';
      this.out.out4 = this.val_CE7.bit1 & this.val_CE7.bit2 | this.val_CE7.bit3;
      this.out.out5 = this.val_CE7.bvec1 == 0x4567;
      this.out.out6 = this.val_CE7.val1 < this.val_CE7.val2 && (this.val_CE7.bvec1 & this.val_CE7.val1) > 0x0005;
      this.out.oVal1 = this.val_CE7.val1 + this.val_CE7.val2;
      this.out.oVal2 = this.val_CE7.val1 + this.val_CE7.bvec2;
      //this.out.oVal3 = this.val.bvec1 - this.val.bvec2; //faulty, arithmetic with BIT_VECTOR
      
      this.out.bVal2 = this.val_CE7.bvec2 >> 2;
      //this.out.bVal3 = (this.val.val1 << 3) + 5;        //faulty: artitmetic with BIT_VECTOR
      this.out.bVal3 = (this.val_CE7.bvec2 << 3) | 0x0003;
      
      this.out.out7 = Fpga.getBit(this.val_CE7.val1, 5);
      this.out.oVec81 = Fpga.getBits(this.val_CE7.val1, 13, 6);
      this.out.oVec82 = 
          Fpga.concatBits(8, this.out.oVec81, 5, Fpga.getBits(this.out.oVec82, 5,1));
      this.out.oVal4 = Fpga.getBitsShL(this.val_CE7.val1, 15, false);
      this.out.oVal5 = Fpga.getBitsShR(true, 15, this.val_CE7.val1);
      this.out.oVal6 = 
          Fpga.concatBits(16, this.out.oVec81, 8, this.out.oVec81);                       // (1)
      this.out.oVal3 = 
          Fpga.concatBits(16, this.out.oVec81, 8, Fpga.getBits(this.out.oVec82, 6,1));    // (2)
      //this.out.oVal6 = 
      //    Fpga.concatBits(16, this.out.oVec81, 10, this.out.oVec82, 6, this.out.oVec83);  // (3)
      //this.out.oVal6 = 
      //    Fpga.concatBits(16, 0b000, 13, this.out.oVec81, 5, this.out.oVal4);             // (4)         
      //this.out.oVal6 = 
      //    Fpga.concatBits(16, this.out.oVec81, 5, this.out.oVal4);                        // (5)
      this.out.oVal8 = 
          Fpga.concatBits(16, 0b000, 13, this.out.oVec81, 6, Fpga.getBits(this.out.oVec82, 7,2));
      //------------------------------------------- OR all values to get dependencies of all.
      this.out.oPins = this.out.oVal1 | this.out.oVal2 | this.out.oVal3 
                     | this.out.oVal4 | this.out.oVal5 | this.out.oVal6;
    }
}
