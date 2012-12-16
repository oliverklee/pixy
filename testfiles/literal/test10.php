<?php

// array assignment

$x1[1] = 1;
$x1[2][1] = 2;
$x1 = array();
~_hotspot0;     // x1:NULL, x1[1]:T, x1[2]:T, x1[2][1]:T

// NOTE: more precise would be: set the whole subtree to null
// (reason: the if-tests below all evaluate to true);
// currently not realised (due to temporary that is generated
// between $x1 and array())
/*
if ($x1 == null) {
    echo 'null! ';
}
if ($x1[1] == null) {
    echo 'null! ';
}
if ($x1[2] == null) {
    // not only leaves become null
    echo 'null! ';
}
*/


?>
