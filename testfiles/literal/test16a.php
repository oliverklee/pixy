<?php

// FORMAL MUST, variant from the journal paper

a();
 
function a() { 
  $a1 = 1;     // a1:1
  $a2 =& $a1;  // a1:1, a2:1
  b(&$a1);                
  ~_hotspot0;  // a1:7, a2:7
}
 
function b(&$bp1) { // bp1:1, bp1_fs:1
  $bp1 = 7;         // bp1:7, bp1_fs:7
  $b2 = 2;          // bp1:7, bp1_fs:7, b2:2
  $bp1 =& $b2;      // bp1:2, bp1_fs:7, b2:2
  $bp1 = 8;         // bp1:8, bp1_fs:7, b2:8 
}


?>
