<?php

// FORMAL MUST, try if calls from the main function work properly;
// with redirection of the global

$x1 = 1;
$x2 = 2;
$evil;
a(&$x1);

function a(&$ap1) {
    ~_hotspot0;                 // main.x1:U/C
    $ap1 = $GLOBALS['evil'];    
    ~_hotspot1;                 // main.x1:T/D
    $ap1 = 1;
    ~_hotspot2;                 // main.x1:U/C
    $GLOBALS['x1'] =& $GLOBALS['x2'];   // redirecting the global away
    $ap1 = $GLOBALS['evil'];
    ~_hotspot3;                 // main.x1:U/C
}

?>
