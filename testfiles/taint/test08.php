<?php

// unset statement

$x1 = $evil;
$x2 =& $x1;
unset($x2);
~_hotspot0;     // x1:T/D, x2:U/C


?>