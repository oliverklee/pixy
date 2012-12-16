<?php

// basic test for interprocedural preservation of globals,
// local perservation of locals and interprocedural deletion of locals;
// here: only may-aliases;
// includes g-shadows (compare with old version below)


if ($u) {
    $x =& $y;
}
a();
~_hotspot0;     // u{} a{(main.x, main.y)}

function a() {      // u{} a{(main.x, main.y)}
                    // u{(main.u, a.u_gs) (main.x, a.x_gs) (main.y, a.y_gs)} 
                    // a{(main.x, main.y) (main.y, a.x_gs) (main.x, a.y_gs) (a.x_gs, a.y_gs)}
    if ($u) {
        $a1 =& $a2;
    }
    b();
    ~_hotspot1;     // u{(main.u, a.u_gs) (main.x, a.x_gs) (main.y, a.y_gs)} 
                    // a{(main.x, main.y) (main.y, a.x_gs) (main.x, a.y_gs) (a.x_gs, a.y_gs) (a.a1, a.a2)}
     
}

function b() {      // u{} a{(main.x, main.y)}
                    // u{(main.u, b.u_gs) (main.x, b.x_gs) (main.y, b.y_gs)} 
                    // a{(main.x, main.y) (main.y, b.x_gs) (main.x, b.y_gs) (b.x_gs, b.y_gs)}
    if ($u) {
        $b1 =& $b2;
    }
    ~_hotspot2;     // u{(main.u, b.u_gs) (main.x, b.x_gs) (main.y, b.y_gs)} 
                    // a{(main.x, main.y) (main.y, b.x_gs) (main.x, b.y_gs) (b.x_gs, b.y_gs) (b.b1, b.b2)}
 
}



/* OLD VERSION: no shadows

if ($unknown) {
    $x =& $y;
}
a();
~_hotspot0;     // u{} a{(main.x, main.y)}

function a() {
    if ($unknown) {
        $a1 =& $a2;
    }
    b();
    ~_hotspot1; // u{} a{(main.x, main.y) (a.a1, a.a2)}
}

function b() {
    if ($unknown) {
        $b1 =& $b2;
    }
    ~_hotspot2; // u{} a{(main.x, main.y) (b.b1, b.b2)}
}

*/

?>
