package org.vishia.fpga.tmpl_J2Vhdl;

import java.io.IOException;

import org.vishia.fpga.Fpga;
import org.vishia.fpga.stdmodules.Bit_ifc;
import org.vishia.fpga.stdmodules.Reset_Inpin_ifc;
import org.vishia.fpga.testutil.TestSignalRecorder;

public class MyFpgaIo implements org.vishia.fpga.FpgaModule_ifc {
  public static class Input {
    public boolean reset_Pin = true;
    public char tristatePin = 'L';
  }
  public static class Output {
    public char tristatePin;
    public boolean testout;
    public boolean testOutFromModule;
    public @Fpga.BITVECTOR(16) int data;
  }
  public final Input input = new Input();
  public final Output output = new Output();
  
  private static class Ref {
    final MyFpgaTop fpga;
    Ref(MyFpgaTop fpga){ this.fpga = fpga; }
  }
  final Ref ref;
  
  @Fpga.IfcAccess public Reset_Inpin_ifc resetPin = new Reset_Inpin_ifc() {
    @Override public boolean reset_Pin () { return MyFpgaIo.this.input.reset_Pin;}
  };
  
  @Fpga.IfcAccess public Bit_ifc specificIfcAccess = new Bit_ifc() {
    @Override public boolean getBit () { return MyFpgaIo.this.input.tristatePin == '1';}
  };
  
  /**This process has no own data, because it should set only the outputs with the CLK.
   * @author hartmut
   *
   */
  @Fpga.VHDL_PROCESS static final class Output_data {
    Output_data (MyFpgaIo thism) {
      thism.output.data = 0;
    }
    @Fpga.VHDL_PROCESS Output_data(int time, Ref ref, MyFpgaIo thism) {
      Fpga.checkTime(time, ref.fpga.modules.mdl1.out.time_ce0, 10);
      Fpga.checkTime(time, ref.fpga.modules.mdl1.out.time_ce7, 3);
      if(ref.fpga.modules.mdl1.srcCE0.ce()) {  // (1) assigns this module to the CE0 time group
        Fpga.checkTime(time, thism.ref.fpga.modules.mdl1.srcCE7, 3);
        thism.output.data = ref.fpga.modules.mdl1.out.oPins;
      } else {
        throw new IllegalStateException();
      }
    }
  }
  Output_data output_data = new Output_data(this);
  
  MyFpgaIo(MyFpgaTop fpga){
    this.ref = new Ref(fpga);
  }
  

  @Override public void reset () {
    // TODO Auto-generated method stub
    
  }

  int time;
  
  @Override public void step ( int time ) {
    // Note for simulation: only save the time for update().
    this.time = time;
  }

  @Override public void update () {
    // Note for simulation:
    // The ctor of output_data does update the ports with the clock.
    if(this.ref.fpga.modules.mdl1.srcCE0.ce()) {
      this.output_data = new Output_data(this.time, this.ref, this);
    }
    
  }
  
  public void output () {
    this.output.tristatePin = this.ref.fpga.modules.mdl1.out.olg1;
  }


  //... preparition of test outputs
  /**This class contributes to assembly of test signals.
   */
  public class TestSignals extends TestSignalRecorder {

    public TestSignals(String moduleName) {
      super(moduleName);
    }

    @Override public void registerLines () {
      // TODO Auto-generated method stub
      
    }

    @Override public int addSignals ( int time, int lenCurr, boolean bAdd ) throws IOException {
      // TODO Auto-generated method stub
      return 0;
    }
  }

}
