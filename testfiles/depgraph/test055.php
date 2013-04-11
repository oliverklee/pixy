<? //

// example from the journal paper (mybloggie)

// optionally comment in/out the following three lines
$tbstatus = "";
$tbreply = "";
$post_urls = $get;

if ($rand) {
    multi_tb($post_urls);
    $tbstatus = $tbstatus . $tbreply;
} else {
    $tbstatus = "";
}

message($tbstatus);

function message($message) {
    echo $message;
}

function multi_tb($post_urls) {
    global $tbreply;
    $tb_urls = split('( )+', $post_urls, 10);
    foreach ($tb_urls as $tb_url) {
        $tbreply .= $tb_url;
    }
}
?>