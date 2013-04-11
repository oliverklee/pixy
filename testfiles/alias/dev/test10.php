<?php

// must-aliases between formals (2)

a();

function a() {
    b(&$a1, &$a1);
}

function b(&$bp1, &$bp2) {
    ~_hotspot0;     // bp1 and bp2 are must-aliases here:
                    // u{(b.bp1, b.bp1_fs, b.bp2, b.bp2_fs) }
                    // a{}
}

/*

// aliases between formals and globals

*/
?>