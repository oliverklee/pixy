<?php

// CALL STRING ANALYSIS: MUST NOT USE FUNCTION SUMMARIES;
// otherwise, there would already be a summary for function a
// by the time the propagation through the long if-branch reaches
// the function call, and would falsely re-use the already 
// computed values main.x1:1 and main.x2:1 that resulted from
// the propagation outside the if-branch


$x1 = 1;
$x2 = 1;
if ($u) {
    $x2 = 2;
    echo 'hi';
    echo 'hi';
    echo 'hi';
    echo 'hi';
    echo 'hi';
}

a();
~_hotspot0;     // main.x1:T, main.x2:T

function a() {
    echo 'hi';
    $GLOBALS['x1'] = $GLOBALS['x2'];
}



?>
