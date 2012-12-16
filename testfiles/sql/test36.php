<? //

// was an inaccuracy:
// because of an SCC (cycle in the depgraph), an uninitialized local function variable
// was considered as tainted

SelectTopNavCats(8);

function SelectTopNavCats($id) {
    $sql = mysql_query($id);
    while ($row=mysql_fetch_array($sql)) {
        $c_catid = $row;
    }
    if (rand()) {
        SelectTopNavCats($c_catid);
    }
}





?>
