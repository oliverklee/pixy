<?php

// $GLOBALS

$x; $y;
a();
~_hotspot0;     // u{(main.x, main.y)} a{}

function a() {
    $GLOBALS['x'] =& $GLOBALS['y'];
}
?>