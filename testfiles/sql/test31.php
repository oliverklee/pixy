<? //

$x = mysql_real_escape_string($get);
deleteCategory($x);
function deleteCategory($category_id) {
	$result = mysql_query('S' . $category_id);
	while ($row = mysql_fetch_array($result)) {
		deleteCategory($row["ID"]);
	}
}





?>
