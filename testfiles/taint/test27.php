<?php

// return values (3)
// functions that have no return statements return NULL implicitly (U/C)

$x1 = $evil;
$x1 = a();
~_hotspot0;     // main.x1:U/C

function a() {
    echo 'do nothing';
}


?>
