<?php

// was a bug

while (list($x1) = foo()) {
    ~_hotspot0;   // x1:U/C
}

function foo() {
    $f1 = array();
    return $f1;
}

?>