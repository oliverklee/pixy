<?php

// return values (2):
// functions that have no return statements return NULL implicitly

$x1 = 1;
$x1 = a();
~_hotspot0;     // main.x1:NULL

function a() {
    echo 'do nothing';
}



?>
