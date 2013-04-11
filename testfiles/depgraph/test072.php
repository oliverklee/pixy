<? //

// passing arrays as parameters,
// trying to be nasty with hidden arrays

$a[1] = $get;
$a[2] = 7;
$b = $a;
foo($b);

function foo($fp) {
    $f1 = $fp;
    echo $f1[1];    // from $get
    echo $f1[2];    // 7
    echo $f1[3];    // uninitialized, tainted
}
?>