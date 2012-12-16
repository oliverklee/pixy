<? //

// LATER:
// small limitation of our analysis of the automaton
// (= the analysis that decides whether an indirectly tainted element
// is dangerous or not);
// in this example, there IS a vulnerability because the
// evil input is not enclosed by quotes; the analysis, however, does
// not report this fact

$get = mysql_real_escape_string($get);
$noise = explode('_', "'");     // equals a single quote, but our analysis doesn't know this
$x = " ' {$noise[0]} $get ";
mysql_query($x);






?>
