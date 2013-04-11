<?

// path pruning

if (true) {
    $x1 = 1;
} else {
    $x2 = 2;
}

~_hotspot0;     // x1:U/C, x2:T/D

$t = true;
if ($t) {
    $x3 = 2;
} else {
    $x4 = 3;
}

~_hotspot1;     // x3:U/C, x4:T/D

if (1 < 2) {
    $x5 = 3;
} else {
    $x6 = 4;
}

~_hotspot2;     // x5:U/C, x6:T/D



?>