package org.vishia.fpga.test;


import org.vishia.parseJava.test.TestParseJava;
import org.vishia.parseJava.test.ParseExample.Prc;
import org.vishia.fpga.Fpga;

public class ExmplVHDL {
  @Fpga.VHDL_PROCESS public class Prc {
    
    @Fpga.BITVECTOR(6) int bitv1 = 0b011000;
    @Fpga.BITVECTOR(7) int bitv2 = 0b011000;
    @Fpga.STDVECTOR(6) int stdv1 = 0b011000;
    static final int m_6 = 0x3f; 
    
    int time_1;
    
    int a=1, b=2, c=3, d=4, e=5, f=6, g=7 ,h=8 ,i=9;
    int j=10 ,k=11 ,l=12 ,m=13 ,n=14 ,o=15 ,p=16 ,q=17 ,r=18 ,s=19;
    int t=20 ,u=0 ,v=0 ,w=0 ;
    int x, y, z;
    
    boolean q1 = false, q2 = true, q3= false, q4 = true, q5 = false;
    boolean q6, q7, q8, q9; 
    
    //tag::exmplOperation[]
    /**This operation is used to parse, and then execute the expression via {@link org.vishia.util.CalculatorExpr}
     * inside {@link TestParseJava}. There also this operation is called to compare the self executed result
     * after conversion to RPN with the result calculated from Java execution itself.
     */
    @SuppressWarnings("unqualified-field-access") //because the fields are used for independent calculation is simple names too.
    public void exmplOperation ( int time) {
      boolean t1, t2, t3, t4;
      if(q3==q4);                               // XNOR both are equal, then it is a enable for transfer.
      if(q1 || q2==q3 && q4);                              // 1
      if(!q1 || q2==q3 && !q4);                            // 2
      if(Fpga.getBit(stdv1, 3));
      if(!q1 || q2==q3 && !Fpga.getBit(bitv1,2));           // 4
      if(Fpga.getBit(bitv2, 2)==false);                    //5
      if(q1 && q2 & q3);                                   // speciality: Using & for BIT AND in VHDL, && is for boolean AND
      if(bitv1 == bitv2 && q1 & q2);
      bitv2 = Fpga.setBit(bitv2, 1, true);
      bitv1 = Fpga.setBits(bitv1, 5, 0, bitv2);
      bitv1 = Fpga.setBits(bitv1, 5, 0, bitv2);
      bitv1 = Fpga.setBits(bitv1, 5, 0, bitv2);
      bitv1 = Fpga.setBits(bitv1, 5, 0, bitv2);            //10
      bitv1 = Fpga.setBits(bitv1, 5, 0, bitv2);
      bitv1 = Fpga.setBits(bitv1, 5, 0, bitv2);
      bitv1 = Fpga.setBits(bitv1, 5, 0, bitv2);
      q6 = q1 && q2;
      q7 = q1 & q2;
      q7 = q1 ^ q2;
      q8 = q1 == q2;
      q6 = q1 ? q2 : q3;                                   //20
      q9 = q1 & (t1 = q2 ? q3 : q4) | q5;                    
      q7 = q1 & q2  ? q4 
         : q2 & !q5 ? q3 & q4
         : q5;
      q7 = q1 ? q3 ? q4 : q5 : q2 ? q3 & q4: q5;
      
      
      bitv1 = q1 ? bitv2
            : q2 & (q3 | q4) ? bitv2
            : 0b1100001110101010;
      
      
      bitv1 = q1 & q2 ? 0b011110                           //25
            : Fpga.getBits(bitv2, 5, 0);
        
      //Note: The line numbers are relevant for test output!
      bitv1 = Fpga.setBit(bitv1, 2, q1);                   // 25
      q9 = q1 && Fpga.getBit(stdv1, 2);
      bitv2 = Fpga.setBits(bitv2, 6, 1, Fpga.getBits(bitv1, 5,0));         
      q6 = bitv1 == 0b011000;
      q6 = Fpga.getBits(bitv1, 4, 2) == 0b110;             //30
      q9 = Fpga.getBits(bitv1, 4, 2) == 0b110 && q2;       //
      s = a + (r = b +c);
      q8 = Fpga.getBit(stdv1, 1);                          
      q9 = !Fpga.getBit(stdv1, 1);                         
      q7 = Fpga.getBit(bitv1, 1) && Fpga.getBit(bitv2, 3); //35
      stdv1 = bitv1;                                       
      bitv1 = stdv1;
      q8 = false;                                          
      bitv1 = 0b111000;
      stdv1 = 0b001100;                                    //40
      stdv1 = (stdv1 +1) & m_6;                            
      bitv1 = Fpga.getBitsShR(q1, 5, bitv1);               
      time_1 = time;                                       
      return;
    }
    //end::exmplOperation[]
    
  } //class Prc
  
  public final Prc prc = new Prc();

}
