<? //

// was a bug:
// unreachable code (here: due to unconditional
// recursion) should not crash the depgraph
// construction algorithm

foo($x);
recrec();
bob($y);


function bob($p) {
    foo($p);
}

function foo($dir) {
    bar($dir);
}

function bar($fname) {
	echo($fname);
}

// unconditional recursion
function recrec() {
    recrec();
}






?>
