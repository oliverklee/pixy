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

~_hotspot0;     // x1:h/h; x2:h/h; x3:h/h


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

~_hotspot1;     // x1:h/h, x4:h/h, x5:h/h, x6:{(h),(u)}/{(h),(u)}

$x1 = $x6;
~_hotspot2;     // x1:{(h),(x6,42)}/{(h),(x6/42)}, x4:the_same, x5:the_same, 
                // x6:{(<harmless>,none)(<uninit>,none)($x6,42)}/repeat



?>
