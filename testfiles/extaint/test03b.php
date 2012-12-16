<?php

// simple assignments, leftCase 2

// *********************************************************************************
// left variable is an array, but not an array element    **************************
// *********************************************************************************

// => there are no aliases for the left variable
// => we don't have to care about MI variables

// a lot of testing has already been done for literals analysis;
// works analogously (apart from caFlag)




$good = array();
$evil;

// right: clean array (and no array element)

$x1;                // x1:u/u
$x2 = array();      // x2:h/h

$x1[1]; $x1[2];
$x2[1]; $x2[3]; $x2[3][1];

$x1 = $x2;
~_hotspot0;         // x1:h/h, x1[1]:h/h, x1[2]:h/h,
                    // x2:h/h, x2[1]:h/h, x2[3]:h/h

// right: dirty array (and no array element)

$x1 = $x3;
~_hotspot1;         // x1:T/D, x1[1]:T/D, x1[2]:T/D

// right: clean array (and an element)

$x1 = $x2[3];
~_hotspot2;         // x1:U/C, x1[1]:U/C, x1[2]:U/C

// right: dirty array (and an element)
$x2[3][1] = $evil;
$x1 = $x2[3];
~_hotspot3;         // x1:U/D, x1[1]:T/D, x1[2]:T/D
                    // (as to x1: we don't care for the hyperspace; in reality, we don't
                    // care for the taint mapping of known arrays; what matters is
                    // their caFlag)



?>


