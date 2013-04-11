<?php

// FORMAL MUST, try if calls from the main function work properly;
// with redirection of the formal

$x1 = 1;
$evil;
a(&$x1);

function a(&$ap1) {
    ~_hotspot0;     // main.x1:U/C, a.ap1:U/C, a.a2:U/C
    $ap1 = $GLOBALS['evil'];
    ~_hotspot1;     // main.x1:T/D, a.ap1:T/D, a.a2:U/C
    $a2 = 3;        // main.x1:T/D, a.ap1:T/D, a.a2:U/C
    $ap1 =& $a2;    // redirecting the formal away
                    // main.x1:T/D, a.ap1:U/C, a.a2:U/C
    $ap1 = 4;       // main.x1:T/D, a.ap1:U/C, a.a2:U/C
    ~_hotspot2;     // main.x1:T/D, a.ap1:U/C, a.a2:U/C
}

?>