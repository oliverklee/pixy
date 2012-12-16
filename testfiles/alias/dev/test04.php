<?php

// basic test for interprocedural preservation of globals,
// local perservation of locals and interprocedural deletion of locals
// here: only must-aliases;
// includes g-shadows (compare with old version below)

$x =& $y;
a();
~_hotspot0;     // u{(main.x, main.y)} a{}

function a() {
    $a1 =& $a2;
    b();
    ~_hotspot1; // u{(main.x, main.y, a.x_gs, a.y_gs) (a.a1, a.a2)} a{}
}

function b() {
    $b1 =& $b2;
    ~_hotspot2; // u{(main.x, main.y, b.x_gs, b.y_gs) (b.b1, b.b2)} a{}
}




/* OLD VERSION: no shadows

$x =& $y;
a();
~_hotspot0;     // u{(main.x, main.y)} a{}

function a() {
    $a1 =& $a2;
    b();
    ~_hotspot1; // u{(main.x, main.y) (a.a1, a.a2)} a{}
}

function b() {
    $b1 =& $b2;
    ~_hotspot2; // u{(main.x, main.y) (b.b1, b.b2)} a{}
}

*/

?>
