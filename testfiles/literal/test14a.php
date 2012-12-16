<?php

// GLOBAL MUST, with shadow-explaining redirection, variant

$x1 = 1;
$x2 = 2;
a();
 
function a() {          
  $a1 =& $GLOBALS['x1'];  // a1:1, x1:1
  b();                
  ~_hotspot0;                   // a1:7, x1:8, x2:8
}
 
function b() {   
  $GLOBALS['x1'] = 7;     // x1:7, x1_gs:7
  $GLOBALS['x1'] =& $GLOBALS['x2'];       // x1:2, x1_gs:7
  $GLOBALS['x1'] = 8;     // x2:8, x1_gs:7
}



?>

