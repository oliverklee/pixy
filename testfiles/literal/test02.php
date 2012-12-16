<?php

// simple assignments, leftCase 1

// *********************************************************************************
// left variable is a "normal" variable ********************************************
// *********************************************************************************


// no additional aliases --------------------------------------------

// right: literal
$x1 = 1;

// right: another normal variable
$x2 = $x1;

// right: myself
$x3 = 2;
$x3 = $x3;

// right: array & array element (later #1)

~_hotspot0;     // x1:1; x2:1; x3:2


// additional must- and may-aliases ---------------------------------

$x4 =& $x1;     // the test for x4:1 here belongs to AssignRef
$x1 = 5;

$x5 = 5;
$x6 = 6;
if ($u) {
    $x5 =& $x1;
    $x6 =& $x1;
}
$x1 = 5;

~_hotspot1;     // x1:5, x4:5, x5:5, x6:T

?>
