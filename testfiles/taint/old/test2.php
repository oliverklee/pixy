<?php

// arrays without non-literal indices on one or on both
// sides of a simple assignment

// pseudo-declarations (to get an overview)
$a[1]; $a[2];
$b[1];

$a[1] = 7;
~_hotspot0;  // $a[1] is untainted, $a is still tainted

$b[2] = 8;

$b = $a;
~_hotspot1;  // $b[1] is untainted, $b[2] is tainted

$b = $c;
~_hotspot2;  // $b[1] and $b[2] are tainted


?>