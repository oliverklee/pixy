<? //

// passing arrays as parameters

$a[1] = $get;
$a[2] = 7;
foo($a);

function foo($fp) {
    echo $fp[1];    // from $get
    echo $fp[2];    // 7
    echo $fp[3];    // uninitialized, hence tainted
}
?>