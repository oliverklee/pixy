<? //

// combination of depth assembly and hidden arrays

$a = array();
$b = array();
$c[2] = $get;
$b = $c;
$a[1] = $b;
echo $a[1][2];   // tainted, from $get






?>
