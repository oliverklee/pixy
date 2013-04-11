<?php

// special superglobals $_TAINTED and $_UNTAINTED
// (used inside the builtin functions file)

$x1 = $_TAINTED;
$x2 = $_UNTAINTED;
~_hotspot0;         // main.x1:T/D, main.x2:U/C

a();

function a() {
    $a1 = $_TAINTED;
    $a2 = $_UNTAINTED;
    ~_hotspot1;     // a.a1:T/D, a.a2:U/C
}


?>