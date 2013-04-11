<?php

// another simple test for reference assignment

if ($u) {
    $x =& $y;
}
$x =& $y;
~_hotspot0;     // u{(x,y)} a{}

?>