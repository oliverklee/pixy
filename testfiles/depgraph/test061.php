<? //

// no info loss through hidden arrays any longer

$a = array();
$b = array();
$c = array();
$c[1] = $get;
$b = $c;    // $b has no explicit index 1
$a = $b;
echo $a[1];   // tainted, from $get






?>
