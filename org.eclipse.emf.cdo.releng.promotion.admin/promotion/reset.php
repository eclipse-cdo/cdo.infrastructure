<?php
ini_set('display_errors', true);

$publicFolder = "/tmp/promotion.emf.cdo/public";

echo '<font face="Helvetica,Arial">';
echo "<h1>Resetting $publicFolder</h1>";
echo '</font>';

deleteFolder("$publicFolder/tasks");
deleteFolder("$publicFolder/tasks.tmp");
deleteFolder("$publicFolder/tasks.inprogress");

function deleteFolder($tmpPath)
{
	if (!is_dir($tmpPath))
	{
		return;
	}

	if (!is_writeable($tmpPath))
	{
		chmod($tmpPath,0777);
	}

	$handle = opendir($tmpPath);
	while ($tmp = readdir($handle))
	{
		if($tmp != '..' && $tmp != '.' && $tmp != '')
		{
			if (is_writeable($tmpPath.DS.$tmp) && is_file($tmpPath.DS.$tmp))
			{
				unlink($tmpPath.DS.$tmp);
			}
			elseif (!is_writeable($tmpPath.DS.$tmp) && is_file($tmpPath.DS.$tmp))
			{
				chmod($tmpPath.DS.$tmp, 0666);
				unlink($tmpPath.DS.$tmp);
			}

			if (is_writeable($tmpPath.DS.$tmp) && is_dir($tmpPath.DS.$tmp))
			{
				deleteFolder($tmpPath.DS.$tmp);
			}
			elseif (!is_writeable($tmpPath.DS.$tmp) && is_dir($tmpPath.DS.$tmp))
			{
				chmod($tmpPath.DS.$tmp, 0777);
				deleteFolder($tmpPath.DS.$tmp);
			}
		}
	}

	closedir($handle);
	rmdir($tmpPath);
}

?>
