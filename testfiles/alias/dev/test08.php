<?php

// f-shadow test;
// redirecting a formal parameter away has no influence on
// the actual parameter

$x;
a();

function a() {
    b(&$a1);
    ~_hotspot0;     // a1 is NOT an alias of main.x:
                    // u{(main.x, a.x_gs)}
                    // a{}
}

function b(&$bp) {  // u{} a{}
                    // u{(main.x, b.x_gs) (b.bp, b.bp_fs)} a{}
    ~_hotspot1;
    global $x;
    $bp =& $x;
}



?>
