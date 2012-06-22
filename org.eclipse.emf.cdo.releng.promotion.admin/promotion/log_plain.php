<?php
// ini_set('display_errors', true);

echo "<tt>\n\n";

$logfile = "/tmp/promotion.emf.cdo/promoter.log";
$content = file_get_contents($logfile);
$content = str_replace('\n', '<br>', $content);
echo $content;

echo "</tt>\n";

?>
