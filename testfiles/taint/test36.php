<?php

// taint analysis with dummy literal analysis 

if (true) {
    $x1 = 7;
}
if (false) {
    $x2 = 7;
}
if ($u) {
    $x3 = 7;
}
$x4 = 7;
if ($v) {
    $x4 = $evil;
}
   
~_hotspot0;     // x1:U/C, x2:T/D, x3:T/D, x4:T/D

?>
