<? //

// was a bug in handling recursive function calls:
// DepTfCallPrep.java deleted the local variables of the
// caller, resulting in a very strange DepGraph

$a = 'hi';
doit($a);

function doit($p) {
	echo($p);
    if ($p) {
		doit($p);
    }
}





?>