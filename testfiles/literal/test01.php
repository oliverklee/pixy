<?php

// default mappings (we always assume that register_globals is active)
// and simple function call

$x;
~_hotspot0;     // main.x:top
a();

function a() {
    $a1;
    ~_hotspot1;     // a.a1:top
}

?>
