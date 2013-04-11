<? //

$a = array();
$b = array();
$b[1] = $get;
$a = $b;
echo $a[1];   // tainted, from $get
?>