<?php

// simple test for reference assignment

if ($u) {
    $x =& $y;
}

$z =& $x;
~_hotspot0;     // u{(z,x)} a{(x,y) (z,y)}

?>
