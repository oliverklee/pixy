<?php

// arrays with non-literal indices, one dimension

// pseudo-declarations (to get an overview)
$a[1]; $a[2]; $a[$i];

// everything is tainted in the beginning
~_hotspot0; // $a is tainted, $a[1] is tainted, $a[2] is tainted, $a[$i] is tainted

// makes everything inside $a untainted, including $a;
$a = array();
~_hotspot1; // $a is untainted, $a[1] is untainted, $a[2] is untainted, $a[$i] is untainted

// makes the specific index tainted, as well as all non-literal indices;
// the analysis doesn't care for $a, so it remains untainted
$a[1] = $b;
~_hotspot2; // $a is untainted, $a[1] is tainted, $a[2] is untainted, $a[$i] is tainted

// makes everything tainted (we don't now it more precisely)
$a[$i] = $b;
~_hotspot3; // $a is untainted, $a[1] is tainted, $a[2] is tainted, $a[$i] is tainted

// doesn't do anything, so everything remains tainted
$a[$i] = 7;
~_hotspot4; // $a is untainted, $a[1] is tainted, $a[2] is tainted, $a[$i] is tainted



?>
