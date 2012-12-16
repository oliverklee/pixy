<?php 

// with kSize == 1:
// make sure that literal analysis doesn't determine the if-condition 
// (and everything afterwards) to be unreachable

a();
a();

if ($x1) {
} 

~_hotspot0;     // must be reachable

function a() {
	b();
}

function b() {
}



?>
