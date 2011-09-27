<?php
ini_set('display_errors', true);

require_once 'Project.php';
require_once 'Drop.php';


$cdo = new Project("CDO", "/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops", array("indigo", "juno"));
$cdo->generate();

?>
