library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use ieee.numeric_std.all;
use ieee.std_logic_unsigned.all;

--tag::Port[]
ENTITY BlinkingLed_Fpga IS
PORT (
  clk: IN BIT;
  reset_Pin : IN BIT;
  led1 : OUT BIT;
  led2 : OUT BIT;
  led3 : OUT BIT  --last line of port definition
);
END BlinkingLed_Fpga;
--end::Port[]

ARCHITECTURE BEHAVIORAL OF BlinkingLed_Fpga IS

--tag::BlinkingLedCt_Q_REC[]
TYPE BlinkingLedCt_Q_REC IS RECORD
  ctLow : STD_LOGIC_VECTOR(15 DOWNTO 0);
  ct : STD_LOGIC_VECTOR(7 DOWNTO 0);
  led : BIT;
END RECORD BlinkingLedCt_Q_REC;
--end::BlinkingLedCt_Q_REC[]

--tag::ClockDivider_Q_REC[]
TYPE ClockDivider_Q_REC IS RECORD
  ct : STD_LOGIC_VECTOR(3 DOWNTO 0);
  ce : BIT;
END RECORD ClockDivider_Q_REC;
--end::ClockDivider_Q_REC[]

TYPE Reset_Q_REC IS RECORD
  resetCount : STD_LOGIC_VECTOR(3 DOWNTO 0);
  res : BIT;
END RECORD Reset_Q_REC;

--tag::RecordSignals[]
SIGNAL ce_Q : ClockDivider_Q_REC;
SIGNAL ct_Q : BlinkingLedCt_Q_REC;
SIGNAL res_Q : Reset_Q_REC;
--end::RecordSignals[]

--tag::BlinkingLedConstants[]
CONSTANT BlinkingLed_Fpga_onDuration_BlinkingLed : INTEGER := 10;
CONSTANT BlinkingLed_Fpga_time_BlinkingLed : BIT_VECTOR(7 DOWNTO 0) := x"64";
--end::BlinkingLedConstants[]



BEGIN



ce_Q_PRC: PROCESS ( clk )
BEGIN IF(clk'event AND clK='1') THEN

  IF ce_Q.ct <  "1001" THEN
      ce_Q.ct <=  ce_Q.ct + 1 ;
  ELSE
      ce_Q.ct <=   "0000";
  END IF;
  IF ce_Q.ct =  "0000"  THEN ce_Q.ce  <=  '1'; ELSE ce_Q.ce  <=  '0'; END IF;
END IF; END PROCESS;

--tag::ct_Q_PRC[]
--tag::ct_Q_PRC-ce[]
ct_Q_PRC: PROCESS ( clk )
BEGIN IF(clk'event AND clK='1') THEN

  IF ce_Q.ce='1' THEN
--end::ct_Q_PRC-ce[]  
--tag::ct_Q_PRC-ifcUsg[]
      IF (res_Q.res)='1' THEN
          ct_Q.ct <= TO_STDLOGICVECTOR(BlinkingLed_Fpga_time_BlinkingLed);
--end::ct_Q_PRC-ifcUsg[]
      ELSE
        IF ct_Q.ctLow(15 DOWNTO 13) =  "111"  THEN
            ct_Q.ctLow <=  x"61a7";
            IF ct_Q.ct = x"00"  THEN
                ct_Q.ct <= TO_STDLOGICVECTOR(BlinkingLed_Fpga_time_BlinkingLed);
        ELSE
                ct_Q.ct <=  ct_Q.ct - 1 ;
            END IF;
        ELSE
            ct_Q.ctLow <=  ct_Q.ctLow - 1 ;
        END IF;
      END IF;
      IF ct_Q.ct < BlinkingLed_Fpga_onDuration_BlinkingLed  THEN ct_Q.led  <=  '1'; ELSE ct_Q.led  <=  '0'; END IF;
  ELSE
  END IF;
END IF; END PROCESS;
--end::ct_Q_PRC[]


res_Q_PRC: PROCESS ( clk )
BEGIN IF(clk'event AND clK='1') THEN

  IF reset_Pin = '0' THEN
      res_Q.resetCount <=   "0000";
  ELSE
    IF res_Q.res='1' THEN
        res_Q.resetCount <=  res_Q.resetCount + 1 ;
    ELSE
    END IF;
  END IF;
  IF res_Q.resetCount <  "1110"  THEN res_Q.res  <=  '1'; ELSE res_Q.res  <=  '0'; END IF;
END IF; END PROCESS;

led1 <=  ct_Q.led;
led2  <=  '1' WHEN  (ct_Q.ct(2 DOWNTO 0) =  "000" )  ELSE '0';
led3  <=  '1' WHEN ct_Q.ct(2 DOWNTO 0) /=  "000"  ELSE '0';

END BEHAVIORAL;
