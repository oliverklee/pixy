<?php

// binary assignment

$good = 1;
$evil;

$x1 = $good + $good;    // x1:U/C
$x2 = $good + $evil;    // x2:U/C
$x3 = $good . $evil;    // x3:T/C

~_hotspot0;         // x1:U/C, x2:U/C, x3:T/C

?>
