<? //

// disabled alias analysis:
// bad programming practice: declaring a superglobal as "global";
// should not lead to an exception during local-global replacement

foo();
function foo() {
	global $HTTP_COOKIE_VARS;
}



?>
