<?php

// if you draw the connector graph for this program,
// you will see that for call-string analyses, the value
// entering a call-prep node does not need to be defined at
// the time the transfer function for the call-ret node is
// invoked (see LiteralTfCallRet) => propagate bottom to
// cease propagation in the next step

a(1);
a(2);

c(1);
c(1);

~_hotspot2;         // main.x1:T, main.x2:1

function a($ap1) {
	b($ap1);
    ~_hotspot0;     // main.x1:T
}

function b($bp1) {
    $GLOBALS['x1'] = $bp1;
}


function c($cp1) {
	d($cp1);
    ~_hotspot1;     // main.x2:1
}

function d($dp1) {
    $GLOBALS['x2'] = $dp1;
}



?>