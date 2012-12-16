<? //

// dirty programming, handled correctly:
// it is useless to declare a superglobal variable 'global';
// issues a warning and ignores it

$x = foo();
mysql_query($x);
function foo() {
    global $HTTP_POST_VARS;
    return $HTTP_POST_VARS['x'];
}





?>
