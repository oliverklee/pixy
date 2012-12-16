<? //

// even though there is a loop in the dependence graph, we
// do not want to get a vulnerability report for this obviously
// harmless case; solved with new treatment of SCCs during decoration

$x = 'a';
while ($rand) {
    $x .= 'b';
}
mysql_query($x);


// cross-check with dangerous example

$y = $get;
while ($rand) {
    $y .= 'c';
}
mysql_query($y);




?>
