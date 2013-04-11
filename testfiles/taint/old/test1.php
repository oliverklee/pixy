<?php

// tainting and untainting with simple assignments
// and non-arrays

// assume that register_globals is active

~_hotspot0;  // $a is tainted
$a = 7;
~_hotspot1;  // $a is untainted
$a = $b;
~_hotspot2;  // $a is tainted again

?>