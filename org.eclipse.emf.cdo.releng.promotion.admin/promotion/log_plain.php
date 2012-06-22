<?php
// ini_set('display_errors', true);

$logfile = "/tmp/promotion.emf.cdo/promoter.log";

if (isset($_GET["delete"]))
{
	unlink($logfile);
}

$logsize = filesize($logfile);
if ($logsize != 0)
{
	$del = '<a href="?delete" title="Delete log file"><img src="images/delete.gif"></a>';
}

echo "<h3>Log Size: $logsize Bytes $del</h3>\n\n";
echo "<tt>\n\n";

$content = file_get_contents($logfile);

while (substr($content, 0, 1) == "\n")
{
	$content = substr($content, 1);
}

$content = str_replace("\n", '<br>', $content);
echo $content;

echo "\n\n</tt>\n";

?>
