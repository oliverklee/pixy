<? //

// rather odd example: effectively tries to
// index a literal

$a = array();
$b = array();
$b[7] = 9;
$a[1] = $b[7];
echo $a[1][2];






?>
