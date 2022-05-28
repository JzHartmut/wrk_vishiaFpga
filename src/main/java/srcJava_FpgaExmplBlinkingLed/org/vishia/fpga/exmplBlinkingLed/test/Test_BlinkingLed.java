package org.vishia.fpga.exmplBlinkingLed.test;

import java.io.IOException;

import org.vishia.fpga.exmplBlinkingLed.fpgatop.BlinkingLed_Fpga;
import org.vishia.fpga.exmplBlinkingLed.fpgatop.BlinkingLed_IoAcc;
import org.vishia.fpga.testutil.TestSignalRecorder;
import org.vishia.fpga.testutil.TestSignalRecorderSet;
import org.vishia.util.TestOrg;

public class Test_BlinkingLed {

  
  BlinkingLed_Fpga fpga = new BlinkingLed_Fpga();
  
  TestSignalRecorderSet outH;
  
  
  void test_All ( TestOrg testParent) throws IOException {
    this.outH = new TestSignalRecorderSet();
    this.outH.addRecorder(this.fpga.ref.ct.new TestSignals("ct"));
    this.outH.addRecorder(this.new TestSignals("io"));
    TestOrg test = new TestOrg("testTxSpe", 6, testParent);
    this.outH.clean();
    int time = 0;
    BlinkingLed_IoAcc.setResetPin( this.fpga, true);           // reset inactive high as only one input
    while(++time < 120000000) {
      this.fpga.step(++time);
      this.fpga.update();
      if(this.fpga.ref.ce.q.ce) {             // speed up simulation, only on ce the data are calculated new.
        this.outH.addSignals(time);
      }
    }
    this.outH.output(System.out);
    test.finish();
  }
  
  
  public static void main(String[] args) {
    Test_BlinkingLed thiz = new Test_BlinkingLed();
    TestOrg test = new TestOrg("Test_BlinkingLed", 3, args);
    try {
      thiz.test_All(test);
    }
    catch(Exception exc) {
      System.out.println(exc.getMessage());
      test.exception(exc);
    }
    test.finish();
  }
  
  
  class TestSignals extends TestSignalRecorder {

    StringBuilder sbLedA = new StringBuilder(500);
    StringBuilder sbLedB = new StringBuilder(500);
    
    private char cLedA, cLedB;
    
    public TestSignals(String sModule) {
      super(sModule);
    }

    @Override public void clean() {
      super.clean();
      super.startLine(this.sbLedA, "ledA");
      super.startLine(this.sbLedB, "ledB");
    }
    
    @Override public int addSignals ( int time , boolean bAdd) throws IOException {
      BlinkingLed_Fpga fpga = Test_BlinkingLed.this.fpga;
      if(fpga.ref.ioPins.output.led1) {
        if(fpga.ref.ioPins.output.led1) {
        }  
      }
      this.cLedA = fpga.ref.ioPins.output.led1 ? 'A': '_';
      this.cLedB = fpga.ref.ioPins.output.led2 ? 'B': '_';
//      if(bAdd) {
//        this.sbLed.append(this.cLed);  
//      }
      return 0;  // no own contribution to length, regard add, sub ordinate.
    }
    
    
    @Override protected void endSignals ( int pos) {
      while(this.sbLedA.length() < pos) { this.sbLedA.append(this.cLedA); }
      while(this.sbLedB.length() < pos) { this.sbLedB.append(this.cLedB); }
    }
    
  }
  
}
