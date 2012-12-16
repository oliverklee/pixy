<? //

// cycle in the dependence graph is approximated;
// we could get a higher precision for this, but
// it was not necessary yet

$x = 'a';
while ($get) {
    $x .= 'b';
}
mysql_query($x);






?>
