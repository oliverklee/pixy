<? //

// "depth assembly"

$a = array();
$b = array();
$b[2] = $get;
$a[1] = $b;
echo $a[1][2];   // tainted, from $get
?>