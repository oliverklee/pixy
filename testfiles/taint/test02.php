<?php

// simple assignments, leftCase 1

// *********************************************************************************
// left variable is a "normal" variable ********************************************
// *********************************************************************************


// no additional aliases --------------------------------------------

// right: literal (untainted)
$x1 = 1;

// right: another normal variable
$x2 = $x1;

// right: myself
$x3 = 2;
$x3 = $x3;

// right: array & array element

~_hotspot0;     // x1:U/C; x2:U/C; x3:U/C


// additional must- and may-aliases ---------------------------------

$x4 =& $x1;     // the test for x4:U/C here belongs to AssignRef
$x1 = 5;

$x5 = 5;
$x6;
if ($u) {
    $x5 =& $x1;
    $x6 =& $x1;
}
$x1 = 5;

~_hotspot1;     // x1:U/C, x4:U/C, x5:U/C, x6:T/D

$x1 = $x6;
~_hotspot2;     // x1:T/D, x4:T/D, x5:T/D, x6:T/D



?>