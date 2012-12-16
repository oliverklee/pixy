<?php

// reference assignments

$x1 = 1;
$x2 =& $x1;
~_hotspot0;     // x1:1, x2:1, x3:T

$x3 = 3;
$x2 =& $x3;
~_hotspot1;     // x1:1, x2:3, x3:3


?>
