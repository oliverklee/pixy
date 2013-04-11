<?php

a();
~_hotspot0;     // u{ (main.x1, main.x2) } a{ }

function a() {
    $a1 =& $GLOBALS['x1'];
    b();
    ~_hotspot1;                   // u{ (a.a1, main.x1, main.x2, a.x1_gs) } a{ }
}

function b() {
    $GLOBALS['x2'] =& $GLOBALS['x1'];
}
?>