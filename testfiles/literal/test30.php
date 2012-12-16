<?php

// return values (1)
// note: when returning arrays, precision is lost
// (the elements of the catching variable are all set to TOP);
// [engineering issue]

$x1 = a();
~_hotspot0;     // main.x1:1

function a() {
    return 1;
}

?>
