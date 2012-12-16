<? //

// was an imprecision: in an earlier version, DepGraph construction
// had the default behavior for mysql_query, meaning that the return
// value had a data flow from the parameters; this is inappropriate for
// mysql_query, and resulted in additional cycles in the depgraph in
// examples such as this one; the result was that several prefixes
// of db-queries could not be resolved successfully

$tab = 'a';
getCategoryPath('i');
function getCategoryPath($id) {
    global $tab;
    if ($_GET['c']) {
        return "ROOT";
    }
	$result = mysql_query('S' . $tab . 'W' . $id);
	$tempOutput = getCategoryPath($result);
    return $tempOutput;
}





?>
