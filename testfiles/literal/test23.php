<?php

/*

this test (for literals analysis) sometimes fails (at
hotspot1, a.a1 is computed to be 1 instead of T);
when it does, compare the output with the output given below
(which is the output when the correct results are computed);

REASON:
INDETERMINISM (due to some hidden implementation bug):
alias analysis sometimes doesn't realize that a1 is
a may-alias of main.x2 at hotspot0; strange: if you remove
any of the assignments inside function b, the correct
result is computed

=> see dev/test25 of alias analysis (same structure)

*/


// MAY WITH GLOBALS (4)

$x1 = 1;
$x2 = 1;
a();

function a() {
    $a1 = 1;
    if ($GLOBALS['u']) {
        $a1 =& $GLOBALS['x1'];
    }
    if ($GLOBALS['v']) {
        $a1 =& $GLOBALS['x2'];
    }
    ~_hotspot0;     // main.x1:1, main.x2:1, a.a1:1
    b();
    ~_hotspot1;     // main.x1:1, main.x2:2, a.a1:T
}

function b() {
    $GLOBALS['x1'] = 1;
    $GLOBALS['x2'] = 2;
}

?>
