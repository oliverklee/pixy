<? //

// was a bug:
// $HTTP_GET_VARS (and others) were not defined as superglobals;
// for this reason, Pixy considered it to be a harmless global variable,
// -> missed vulns

$x = foo();
mysql_query($x);

function foo() {
    return $HTTP_GET_VARS['x'];
}







?>
