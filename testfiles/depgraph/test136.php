<? //

// was a bug that led to a false positive

unset($x);    // same effect as array()

$x[0] = 'a';

echo $x[0];   // ok!
echo $x[1];   // ok!
echo $x[$y];  // ok!

foreach($x as $val){
  echo $val;    // ok!
}




?>