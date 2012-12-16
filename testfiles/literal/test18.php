<?php

// FORMAL MUST, try if calls from the main function work properly;
// with redirection of the global

$x1 = 1;
$x2 = 2;
a(&$x1);

function a(&$ap1) {
    $ap1 = 3;
    ~_hotspot0;     // main.x1:3, main.x2:2, a.ap1:3
    $a2 = 4;        // main.x1:3, main.x2:2, a.ap1:3, a.a2:4
    $GLOBALS['x1'] =& $GLOBALS['x2'];   // redirecting the global away
                    // main.x1:2, main.x2:2, a.ap1:3, a.a2:4
    $ap1 = 5;       // main.x1:2, main.x2:2, a.ap1:5, a.a2:4
    ~_hotspot1;     // main.x1:2, main.x2:2, a.ap1:5, a.a2:4
}

?>
