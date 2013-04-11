<?php

// default parameters: simply ignore them since they have no effect on alias information

a();
~_hotspot0;     // u{ (main.x1, main.x2) } a{ }

function a($ap1 = 1) {
    echo 'hi';
    $GLOBALS['x1'] =& $GLOBALS['x2'];
}

?>