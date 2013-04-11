<?php

// apart from the "main message" of this example, we also don't "declare" main.x1 explicitly

a();

function a() {
    $a1 =& $GLOBALS['x1'];
    ~_hotspot0;             // u{ (a.a1, main.x1, a.x1_gs) } a{ }
    b(&$a1);
}

function b(&$bp1) {
    ~_hotspot1;             // u{ (main.x1, b.x1_gs, b.bp1, b.bp1_fs) } a{ }
}
?>