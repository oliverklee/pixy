<?php

// unset statement

$x1 = 1;
$x2 =& $x1;
unset($x2);
~_hotspot0;     // x1:1, x2:NULL


?>
