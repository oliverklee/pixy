<?php

// must-aliases between formals and globals

$x;
a();

function a() {
    global $x;
    $a1 =& $x;
    b(&$a1);
    ~_hotspot0; // nothing happened to the aliasing:
                // u{(main.x, a.x_gs, a.x, a.a1)}
                // a{}
}

function b(&$bp1) {
    ~_hotspot1;     // bp1 is a must-alias of main.x here:
                    // u{(main.x, b.x_gs, b.bp1, b.bp1_fs)}
                    // a{}

}

?>
