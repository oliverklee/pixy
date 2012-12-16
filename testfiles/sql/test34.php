<? //

// was a bug: if a function returns nothing,
// the return value (actually "NULL") should not be considered as tainted

$x = foo();
mysql_query($x);

function foo() {
    $a = 1;
}





?>
