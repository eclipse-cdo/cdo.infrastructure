<?php
// ini_set('display_errors', true);

$logfile = "/tmp/promotion.emf.cdo/promoter.log";

echo '<font face="Helvetica,Arial">';
echo "<h1>$logfile</h1>";
echo '</font>';

echo '<pre>';
echo htmlspecialchars(file_get_contents($logfile));
echo '</pre>';

?>
