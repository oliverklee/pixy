<?php

// don't forget that path pruning is enabled;
// if you want unresolvable conditions, use a global as guard (e.g., $GLOBALS['u'])

$x1 = $evil;
$x2 = 2;
a();

function a() {
    ~_hotspot0;     // main.x1:T/D, main.x2:U/C
    if ($u) {
        // this branch is never entered since $u:null (false)
        $GLOBALS['x2'] =& $GLOBALS['x1'];
    }
    ~_hotspot1;     // main.x1:T/D, main.x2:U/C
}


?>

