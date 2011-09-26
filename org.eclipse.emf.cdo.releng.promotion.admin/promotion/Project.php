<?php

require_once 'Drop.php';

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
		$this->init();
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

	function generate()
	{
		echo "<h1>$name Promotion Admin</h1>";
		echo '<table>';
		foreach ($this->drops as $drop)
		{
			$drop->generate();
		}

		echo '</table>';
	}

	private function init()
	{
		$d = dir($this->path);
		while (false !== ($entry = $d->read()))
		{
			if (strpos($entry, ".") !== 0)
			{
				$this->addDrop($entry);
			}
		}

		$d->close();
	}

	private function addDrop($qualifier)
	{
		$drop = new Drop($this, $qualifier);
		$this->drops[count($this->drops)] = $drop;
		$this->dropsByQualifier[$drop->getQualifier()] = $drop;
		return $drop;
	}
}

?>
