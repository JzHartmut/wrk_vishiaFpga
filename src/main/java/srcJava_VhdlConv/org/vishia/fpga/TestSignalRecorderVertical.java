package org.vishia.fpga;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.vishia.util.StringFormatter;
import org.vishia.util.StringFunctions_C;

public class TestSignalRecorderVertical {

  
  List<TestSignalVerticalAdd> srcList = new LinkedList<TestSignalVerticalAdd>();
  
  private Writer wr;
  
  protected StringFormatter sLine;
  
  String[] sOut;
  
  
  
  public void connect ( ) {
    
  }
  
  
  public void addSrc ( TestSignalVerticalAdd src) {
    this.srcList.add(src);
  }

  final static String empty = "                                            ";

  
  public void write ( int time) throws IOException {
    if(this.sOut == null) { this.sOut = new String[this.srcList.size()]; }
    int ix = 0;
    int changeMax = -1;
    for(TestSignalVerticalAdd src : this.srcList) {
      int change = src.add(this.sOut, ix);
      if(change == 0) { changeMax = ix; }
      if(change >0) {
        this.sOut[ix] = empty.substring(0, change);
      }
      ix +=1;
    }
    ix = 0;
    if(changeMax >=0 ) {
      this.wr.append(Integer.toString(time)).append(":");
    }
    while(changeMax >=ix) {
      this.wr.append(this.sOut[ix]);
      ix +=1;
    }
    if(changeMax >=0 ) {
      this.wr.append("\n");
      this.wr.flush();
    }
  }
  
  
  
  public void open(File file) throws IOException {
    this.wr = new FileWriter(file);
    this.sLine = new StringFormatter(this.wr, false, "\n", 200);
  }
  
  public void close() throws IOException { this.wr.close(); }
  
}
