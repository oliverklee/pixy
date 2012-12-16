<?php

// FORMAL MUST, try if calls from the main function work properly;
// with redirection of the formal

$x1 = 1;
a(&$x1);

function a(&$ap1) {
    $ap1 = 2;
    ~_hotspot0;     // main.x1:2, a.ap1:2
    $a2 = 3;        // main.x1:2, a.ap1:2, a.a2:3
    $ap1 =& $a2;    // redirecting the formal away
                    // main.x1:2, a.ap1:3, a.a2:3
    $ap1 = 4;       // main.x1:2, a.ap1:4, a.a2:4
    ~_hotspot1;     // main.x1:2, a.ap1:4, a.a2:4
}

?>
