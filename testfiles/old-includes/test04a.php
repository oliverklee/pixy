<?php

function taintTheGlobal() {
    global $x1, $evil;
    $x1 = $evil;
}

?>
