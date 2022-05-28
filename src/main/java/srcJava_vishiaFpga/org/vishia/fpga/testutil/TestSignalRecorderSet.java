package org.vishia.fpga.testutil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TestSignalRecorderSet {

  
  
  private List<TestSignalRecorder> recs = new LinkedList<TestSignalRecorder>();
  
  
  
  public void addRecorder ( TestSignalRecorder rec) {
    recs.add(rec);
  }
  
  
  /**Cleans the buffers with only the start line information
   * for new recording. 
   */
  public void clean ( ) {
    for(TestSignalRecorder rec: this.recs) {
      rec.clean();
    }
  }
  
  
  public void addSignals(int time) throws IOException {
    int zLine = 0;
    for(TestSignalRecorder rec: this.recs) {
      boolean bAdd = zLine >0;                             // if any recorder has added somewhat before, then true
      int zLine1 = rec.addSignals(time, bAdd);             // bAdd==false can prevent addition
      if(zLine < zLine1) {                                 // if has added somewhat, zLine is the longest line
        zLine = zLine1;
      }
    }
    if(zLine >0) {
      for(TestSignalRecorder rec: this.recs) {
        rec.endSignals(zLine);
      }
    }
    
  }
  
  
  
  
  public void output(Appendable out) throws IOException {
    for(TestSignalRecorder rec: this.recs) {
      rec.output(out);
    }
  }
  
  
  
  
}
