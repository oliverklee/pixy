<?php

// path pruning

if (true) {
    $x1 = 1;
} else {
    $x2 = 2;
}

$t = true;
if ($t) {
    $x2 = 2;
} else {
    $x2 = 3;
}

if (1 < 2) {
    $x3 = 3;
} else {
    $x4 = 4;
}

~_hotspot0;     // x1:1, x2:2, x3:3


?>
