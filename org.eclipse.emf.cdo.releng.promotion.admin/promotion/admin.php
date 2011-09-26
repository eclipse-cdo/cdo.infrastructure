<?php

print "<h1>CDO Promotion Admin</h1>";


$cdo = new Project("CDO", "/home/data/httpd/download.eclipse.org/modeling/emf/cdo/drops");
foreach ($cdo->getDrops() as $drop)
{
	echo $drop->getQualifier()."<br>\n";
}

class Project
{
	private $name;
	private $path;
	private $drops = array();
	private $dropsByQualifier = array();

	function __construct($name, $path)
	{
		$this->name = $name;
		$this->path = $path;

		$d = dir($path);
		while (false !== ($entry = $d->read()))
		{
			if (strpos($entry, ".") !== 0)
			{
				$this->addDrop($entry);
			}
		}

		$d->close();
	}

	function getName()
	{
		return $this->name;
	}

	function getPath()
	{
		return $this->path;
	}

	function getDrops()
	{
		return $this->drops;
	}

	function getDrop($qualifier)
	{
		return $this->dropsByQualifier[$qualifier];
	}

	function addDrop($qualifier)
	{
		$drop = new Drop($this, $qualifier);
		$this->drops[count($this->drops)] = $drop;
		$this->dropsByQualifier[$drop->getQualifier()] = $drop;
		return $drop;
	}
}

class Drop
{
	private $project;
	private $qualifier;

	function __construct($project, $qualifier)
	{
		$this->project = $project;
		$this->qualifier = $qualifier;
	}

	function getProject()
	{
		return $this->project;
	}

	function getQualifier()
	{
		return $this->qualifier;
	}
}

?>
