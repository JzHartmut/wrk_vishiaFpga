package org.vishia.fpga.stdmodules;

import java.io.IOException;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.FpgaModule_ifc;
import org.vishia.fpga.testutil.TestSignalRecorder;
import org.vishia.util.StringFunctions_C;

public class CrcGeneratorSerial implements FpgaModule_ifc, CrcGenSerial_Outifc {

  
  
  static class Ref {
    CeTime_ifc ce;
    CrcGenSerial_Inpifc inp;
    
    public Ref(CeTime_ifc ce, CrcGenSerial_Inpifc inp) {
      this.ce = ce;
      this.inp = inp;
    }
  } // class Ref
  
  Ref ref;
  
  
  @Fpga.VHDL_PROCESS static class Q {
    
    final @Fpga.BITVECTOR(32) int sh;

    public Q ( ) {
      this.sh = 0;
    }
    
    @Fpga.VHDL_PROCESS public Q ( int time, Q z, Ref ref) {
      final @Fpga.BITVECTOR(32) int sh_d;
      final boolean b0;
      final boolean bin;
      if(ref.ce.ce()) {
        if(ref.inp.stateBeforeCrc(time, 10)) {
          this.sh = 0xffffffff; 
        } 
        else if(ref.inp.stateCrc(time, 10)) {
          this.sh = Fpga.getBitsShR(false, 31, z.sh); //, 31, false);
        }
        else {                       //stateData
          b0 = Fpga.getBit(z.sh, 0);
          bin = ref.inp.getBit(time, 0);
          sh_d = Fpga.getBitsShR(false, 31, z.sh);
          if(b0 != bin) {
            this.sh = sh_d ^ 0xedb88320;  // this is reverse the polynom 0x04c11db7;
          } else {
            this.sh = sh_d;
          }
        }
      }
      else {        // not ce
        this.sh = z.sh;
      }
    }
    
    
  }
  
  Q q = new Q(), q_d = q ;
  
  public CrcGeneratorSerial ( ) {
  } 
  
  public CrcGeneratorSerial ( CeTime_ifc ce, CrcGenSerial_Inpifc inp) {
    this.ref = new Ref(ce, inp);
  } 
  
  
  public void init ( CeTime_ifc ce, CrcGenSerial_Inpifc inp) {
    this.ref = new Ref(ce, inp);
  } 
  
  @Override public void reset ( ) {
    this.q = new Q();
  }

  @Override public void step ( int time ) {
    this.q_d = new Q(time, this.q, this.ref);
  }

  @Override public void update () {
    this.q = this.q_d;
  }

  @Override public boolean getBitCrc ( int time, int min ) {
    return ! Fpga.getBit(this.q.sh, 0);
  }
  
  
  public class TestSignals extends TestSignalRecorder {

    final StringBuilder sbCrc = new StringBuilder(15000);
    
    final boolean bClk;
    boolean bAfterCe;
    
    public TestSignals(String moduleName, boolean bClk) {
      super(moduleName);
      this.bClk = bClk;
    }
    
    @Override public void registerLines () {
      super.clean();
      registerLine(this.sbCrc, "crc");
    }

    @Override public int addSignals ( int time, int lenCurr, boolean bAdd ) throws IOException {
      CrcGeneratorSerial thism = CrcGeneratorSerial.this;
      if(this.bClk) {
        if(this.bAfterCe) {
          StringFunctions_C.appendHex(this.sbCrc, thism.q.sh, 8);
        }
        this.bAfterCe = thism.ref.ce.ce();
      } else {
        char cs = (char)((thism.q.sh >>26) + '0');
        this.sbCrc.append(cs); 
      }
      super.endSignals(super.pos);
      return super.pos;
    }

  }


}
