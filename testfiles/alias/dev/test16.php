<?php

// direct recursion: don't mix up caller locals with callee locals (formals)

a(7, 8);

function a($ap1, $ap2) {

    ~_hotspot0;         // only one context:
                        // u{ (a.ap1, a.ap1_fs) (a.ap2, a.ap2_fs) }  a{ }
    $ap1 =& $ap2;
    if ($u) {
        a(&$ap1, 2);
    }
}
?>