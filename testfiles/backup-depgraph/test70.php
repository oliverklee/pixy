<? //

// returning arrays from functions

$a = foo($get);
echo $a[1];     // 7
echo $a[2];     // uninitialized within foo, hence untainted
echo $a[3];     // from $get

function foo($fp) {
    $f[1] = 7;
    $f[3] = $fp;
    return $f;
}





?>
