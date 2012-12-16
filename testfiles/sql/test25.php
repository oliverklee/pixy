<? //

// evil case that triggered an exception:
// a literal is passed as an actual parameter,
// and the corresponding formal is used as an array element under
// certain conditions; see DepGraph.getUsedPlaces(), case CfgNodeCallPrep

foo(0);
function foo($p) {
    if ($p) {
        mysql_query($p[1]);
    }
}





?>
