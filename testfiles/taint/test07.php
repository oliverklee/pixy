<?php

// reference assignments

$x1 = 1;
$x2 =& $x1;
~_hotspot0;     // x1:U/C, x2:U/C, x3:T/D

$x3;
$x2 =& $x3;
~_hotspot1;     // x1:U/C, x2:T/D, x3:T/D


?>